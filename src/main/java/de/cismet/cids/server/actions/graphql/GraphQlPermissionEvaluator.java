/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.actions.graphql;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import graphql.language.Argument;
import graphql.language.AstPrinter;
import graphql.language.AstTransformer;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;

import graphql.parser.InvalidSyntaxException;
import graphql.parser.Parser;

import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cids.server.actions.graphql.exceptions.FieldNotFoundException;
import de.cismet.cids.server.actions.graphql.exceptions.TableNotFoundException;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GraphQlPermissionEvaluator implements ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(GraphQlPermissionEvaluator.class);

    //~ Instance fields --------------------------------------------------------

    private List<CidsField> fieldsWithoutReadPermission;
    private final MetaService ms;
    private final User user;
    private final ConnectionContext cc;
    private List<String> tablesWithoutPermissionCheck = new ArrayList<>();
    private final String[] FIELD_EXTENSIONS = { "Array", "Object", "ArrayRelationShip" };

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GraphQlPermissionEvaluator object.
     *
     * @param  ms    DOCUMENT ME!
     * @param  user  DOCUMENT ME!
     * @param  cc    DOCUMENT ME!
     */
    public GraphQlPermissionEvaluator(final MetaService ms, final User user, final ConnectionContext cc) {
        this.ms = ms;
        this.user = user;
        this.cc = cc;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   query  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String evaluate(final String query) {
        try {
            fieldsWithoutReadPermission = new ArrayList<CidsField>();
            final Parser p = new Parser();
            final Document doc = p.parseDocument(query);
            final AstTransformer transformer = new AstTransformer();
            final CidsVisitor visitor = new CidsVisitor();
            final Document newDoc = (Document)transformer.transform(doc, visitor);

            return AstPrinter.printAstCompact(newDoc);
        } catch (InvalidSyntaxException e) {
            LOG.error("Error while evaluating graphql query", e);
        }

        return null;
    }

    /**
     * Only for test purposes.
     *
     * @param   query  the query to test
     *
     * @return  true, iff the query was changed by the evaluate method
     */
    public boolean existEvaluateProblems(final String query) {
        final Parser p = new Parser();
        final Document doc = p.parseDocument(query);

        return !AstPrinter.printAstCompact(doc).equals(evaluate(query));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tablesWithoutPermissionCheck  the tablesWithoutPermissionCheck to set
     */
    public void setTablesWithoutPermissionCheck(final List<String> tablesWithoutPermissionCheck) {
        this.tablesWithoutPermissionCheck = tablesWithoutPermissionCheck;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<CidsField> getFieldsWithoutPermissions() {
        return fieldsWithoutReadPermission;
    }

    /**
     * Checks, if the field references to an argument.
     *
     * @param   field          DOCUMENT ME!
     * @param   isWhereClause  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isFieldArgument(final ObjectField field, final boolean isWhereClause) {
        if (field.getValue() instanceof ObjectValue) {
            final ObjectValue f = (ObjectValue)field.getValue();

            if ((f.getObjectFields().size() > 0) && (f.getObjectFields().get(0) instanceof ObjectField)) {
                if (((ObjectField)f.getObjectFields().get(0)).getValue() instanceof ObjectValue) {
                    final ObjectValue val = (ObjectValue)((ObjectField)f.getObjectFields().get(0)).getValue();

                    return !(val.getObjectFields().size() > 0) || (val.getObjectFields().get(0) instanceof ObjectField);
                }
            }

            return true;
        } else {
            return !isWhereClause;
        }
    }

    /**
     * Checks, if the given field references to a table.
     *
     * @param   field          the field to check
     * @param   isWhereClause  is the field within a where clause
     *
     * @return  true, iff the given field references to a table
     */
    private boolean isTable(final ObjectField field, final boolean isWhereClause) {
        if (!isFieldArgument(field, isWhereClause)) {
            if (field.getValue() instanceof ObjectValue) {
                final ObjectValue f = (ObjectValue)field.getValue();

                if ((f.getObjectFields().size() > 0) && (f.getObjectFields().get(0) instanceof ObjectField)) {
                    if (((ObjectField)f.getObjectFields().get(0)).getValue() instanceof ObjectValue) {
                        final ObjectValue val = (ObjectValue)((ObjectField)f.getObjectFields().get(0)).getValue();

                        return (val.getObjectFields().size() > 0)
                                    && (val.getObjectFields().get(0) instanceof ObjectField);
                    }
                }

                return false;
            }
        }

        return false;
    }

    /**
     * Determines the foreign key field that refers to the given destination.
     *
     * @param   table                 DOCUMENT ME!
     * @param   foreignKeyDestinaton  fieldName DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  TableNotFoundException  if given table does not exists or the current user has no read permission for
     *                                  the table
     * @throws  FieldNotFoundException  if given field does not exists or the current user has no read permission for
     *                                  the field
     */
    private CidsField determineForeignKeyField(final CidsTable table, final String foreignKeyDestinaton)
            throws RemoteException, TableNotFoundException, FieldNotFoundException {
        final MetaClass mc = getMetaClassByName(table.getName());

        if (mc == null) {
            throw new TableNotFoundException(table.getName());
        }

        for (final Object attribute : mc.getMemberAttributeInfos().values()) {
            if (attribute instanceof MemberAttributeInfo) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)attribute;

                if (mai.isForeignKey()) {
                    if (mai.getFieldName().equalsIgnoreCase(foreignKeyDestinaton)) {
                        return new CidsField(table, foreignKeyDestinaton);
                    } else {
                        for (final String extension : FIELD_EXTENSIONS) {
                            if (foreignKeyDestinaton.endsWith(extension)) {
                                if (mai.getFieldName().equalsIgnoreCase(
                                                foreignKeyDestinaton.substring(
                                                    0,
                                                    foreignKeyDestinaton.length()
                                                    - extension.length()))) {
                                    return new CidsField(
                                            table,
                                            foreignKeyDestinaton.substring(
                                                0,
                                                foreignKeyDestinaton.length()
                                                        - extension.length()));
                                }
                            }
                        }
                    }
                }
            }
        }

        for (final Object attribute : mc.getMemberAttributeInfos().values()) {
            if (attribute instanceof MemberAttributeInfo) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)attribute;

                if (mai.isForeignKey()) {
                    final MetaClass referencedTable = getMetaClassById(mai.getForeignKeyClassId());

                    if (referencedTable != null) {
                        if (referencedTable.getTableName().equalsIgnoreCase(foreignKeyDestinaton)) {
                            return new CidsField(table, mai.getFieldName());
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table                 DOCUMENT ME!
     * @param   foreignKeyDestinaton  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  TableNotFoundException  DOCUMENT ME!
     * @throws  FieldNotFoundException  DOCUMENT ME!
     */
    private String determineTableForForeignKeyField(final CidsTable table, final String foreignKeyDestinaton)
            throws RemoteException, TableNotFoundException, FieldNotFoundException {
        final MetaClass mc = getMetaClassByName(table.getName());
        if (mc == null) {
            throw new TableNotFoundException(table.getName());
        }

        for (final Object attribute : mc.getMemberAttributeInfos().values()) {
            if (attribute instanceof MemberAttributeInfo) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)attribute;

                if (mai.isForeignKey()) {
                    if (mai.getFieldName().equalsIgnoreCase(foreignKeyDestinaton)) {
                        final MetaClass tabMeta = getMetaClassById(mai.getForeignKeyClassId());

                        if (tabMeta != null) {
                            return tabMeta.getTableName();
                        }
                    } else {
                        boolean fieldFound = false;

                        for (final String extension : FIELD_EXTENSIONS) {
                            if (foreignKeyDestinaton.endsWith(extension)) {
                                if (mai.getFieldName().equalsIgnoreCase(
                                                foreignKeyDestinaton.substring(
                                                    0,
                                                    foreignKeyDestinaton.length()
                                                    - extension.length()))) {
                                    fieldFound = true;
                                }
                            }
                        }

                        if (fieldFound) {
                            final MetaClass tabMeta = getMetaClassById(mai.getForeignKeyClassId());

                            if (tabMeta != null) {
                                return tabMeta.getTableName();
                            }
                        }
                    }
                }
            }
        }

        for (final Object attribute : mc.getMemberAttributeInfos().values()) {
            if (attribute instanceof MemberAttributeInfo) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)attribute;

                if (mai.isForeignKey()) {
                    final MetaClass referencedTable = getMetaClassById(mai.getForeignKeyClassId());

                    if (referencedTable != null) {
                        if (referencedTable.getTableName().equalsIgnoreCase(foreignKeyDestinaton)) {
                            return referencedTable.getTableName();
                        }
                    }
                }
            }
        }

        throw new TableNotFoundException(foreignKeyDestinaton);
    }

    /**
     * Checks if current user has read permissions for the given field.
     *
     * @param   table      the table to check
     * @param   fieldName  the field to check
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  TableNotFoundException  if the given table does not exists or the current user has no read permission
     *                                  for the table
     * @throws  FieldNotFoundException  if the given field does not exists or the current user has no read permission
     *                                  for the field
     */
    private void checkReadPermission(final CidsTable table, final String fieldName) throws RemoteException,
        TableNotFoundException,
        FieldNotFoundException {
        if (tablesWithoutPermissionCheck.contains(table.getName())) {
            return;
        }
        final MetaClass mc = getMetaClassByName(table.getName());

        if (mc == null) {
            throw new TableNotFoundException(table.getName());
        }

        for (final Object attribute : mc.getMemberAttributeInfos().values()) {
            if (attribute instanceof MemberAttributeInfo) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)attribute;

                if (mai.getFieldName().equalsIgnoreCase(fieldName)) {
                    return;
                } else {
                    for (final String extension : FIELD_EXTENSIONS) {
                        if ((fieldName.length() > extension.length())
                                    && mai.getFieldName().equalsIgnoreCase(
                                        fieldName.substring(0, fieldName.length() - extension.length()))) {
                            return;
                        }
                    }
                }
            }
        }

        for (final String extension : FIELD_EXTENSIONS) {
            if (fieldName.endsWith(extension)) {
                checkReadPermission(table, fieldName.substring(0, fieldName.length() - extension.length()));
            }
        }

        for (final Object attribute : mc.getMemberAttributeInfos().values()) {
            if (attribute instanceof MemberAttributeInfo) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)attribute;

                if (mai.isForeignKey()) {
                    final MetaClass referencedTable = getMetaClassById(mai.getForeignKeyClassId());

                    if (referencedTable != null) {
                        if (referencedTable.getTableName().equalsIgnoreCase(fieldName)) {
                            return;
                        }
                    }
                }
            }
        }

        throw new FieldNotFoundException(table.getName(), fieldName);
    }

    /**
     * Checks if current user has write permissions for the given field.
     *
     * @param   table      the table to check
     * @param   fieldName  the field to check
     *
     * @return  true, iff the user has write permissions
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  TableNotFoundException  if the given table does not exists or the current user has no read permission
     *                                  for the table
     * @throws  FieldNotFoundException  if the given field does not exists or the current user has no read permission
     *                                  for the field
     */
    private boolean hasWritePermission(final CidsTable table, final String fieldName) throws RemoteException,
        TableNotFoundException,
        FieldNotFoundException {
        if (tablesWithoutPermissionCheck.contains(table.getName())) {
            return true;
        }
        final MetaClass mc = getMetaClassByName(table.getName());

        if (mc == null) {
            throw new TableNotFoundException(table.getName());
        }

        for (final Object attribute : mc.getMemberAttributeInfos().values()) {
            if (attribute instanceof MemberAttributeInfo) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)attribute;
                boolean fieldFound = false;

                if (mai.getFieldName().equalsIgnoreCase(fieldName)) {
                    fieldFound = true;
                } else {
                    for (final String extension : FIELD_EXTENSIONS) {
                        if (fieldName.endsWith(extension)) {
                            if (mai.getFieldName().equalsIgnoreCase(
                                            fieldName.substring(0, fieldName.length() - extension.length()))) {
                                fieldFound = true;
                            }
                        }
                    }
                }

                if (fieldFound) {
                    if (mc.getPermissions().hasWritePermission(user)) {
                        // todo the write permission for the field should be checked too
                        return !mai.isExtensionAttribute();
                    } else {
                        return false;
                    }
                }
            }
        }

        for (final String extension : FIELD_EXTENSIONS) {
            if (fieldName.endsWith(extension)) {
                hasWritePermission(table, fieldName.substring(0, fieldName.length() - extension.length()));
            }
        }

        for (final Object attribute : mc.getMemberAttributeInfos().values()) {
            if (attribute instanceof MemberAttributeInfo) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)attribute;

                if (mai.getFieldName().equalsIgnoreCase(fieldName)) {
                    if (mc.getPermissions().hasWritePermission(user)) {
                        return !mai.isExtensionAttribute();
                    } else {
                        return false;
                    }
                }
            }
        }

        throw new FieldNotFoundException(table.getName(), fieldName);
    }

    /**
     * Checks if current user has write permissions on the given table.
     *
     * @param   table  the table to check
     *
     * @return  true, iff the user has write permissions
     *
     * @throws  RemoteException         DOCUMENT ME!
     * @throws  TableNotFoundException  if the given table does not exists or the current user has no read permission
     *                                  for the table
     */
    private boolean hasWritePermission(final CidsTable table) throws RemoteException, TableNotFoundException {
        if (tablesWithoutPermissionCheck.contains(table.getName())) {
            return true;
        }
        final MetaClass mc = getMetaClassByName(table.getName());

        if (mc == null) {
            throw new TableNotFoundException(table.getName());
        } else {
            return mc.getPermissions().hasWritePermission(user);
        }
    }

    /**
     * Returns the corrsponding meta class.
     *
     * @param   name  the table name of the meta class
     *
     * @return  the corresponding meta class
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    private MetaClass getMetaClassByName(final String name) throws RemoteException {
        MetaClass mc = ms.getClassByTableName(user, name, cc);

        if (mc == null) {
            mc = ms.getClassByTableName(user, name.toLowerCase(), cc);
        }

        for (final String extension : FIELD_EXTENSIONS) {
            if ((mc == null) && name.endsWith(extension)) {
                mc = ms.getClassByTableName(user, name.substring(0, name.indexOf(extension)), cc);
            }
        }

        return mc;
    }

    /**
     * Returns the corrsponding meta class.
     *
     * @param   id  the id of the meta class
     *
     * @return  the corresponding meta class
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    private MetaClass getMetaClassById(final int id) throws RemoteException {
        return ms.getClass(user, id, cc);
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return cc;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CidsVisitor extends NodeVisitorStub {

        //~ Instance fields ----------------------------------------------------

        private final List<String> readArguments = new ArrayList<>();
        private final List<String> operators = new ArrayList<>();
        private final List<CidsDataSource> fieldsWithoutPermission = new ArrayList<>();
        private CidsTable lastTable = null;
        private Map<String, CidsTable> fieldToTable = new HashMap<>();
        private OperationDefinition.Operation operation = null;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ReadVisitor object.
         */
        public CidsVisitor() {
            readArguments.add("where");
            readArguments.add("order_by");
            operators.add("_and");
            operators.add("_not");
            operators.add("_or");
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public TraversalControl visitArgument(final Argument node,
                final TraverserContext<Node> context) {
            return TraversalControl.CONTINUE;
        }

        @Override
        public TraversalControl visitObjectField(final ObjectField node, final TraverserContext<Node> context) {
            String fieldName = node.getName();
            final Argument argument = getArgument(context.getParentNodes());
            final String argumentName = ((argument != null) ? argument.getName() : null);
            String parentFieldName = getParentTableName(context.getParentNodes());

            if (operation.equals(OperationDefinition.Operation.MUTATION)) {
                if (isSpecialMutationField(fieldName)) {
                    return TraversalControl.CONTINUE;
                }
                fieldName = mutationFieldNameToFieldName(fieldName);
                parentFieldName = mutationFieldNameToFieldName(parentFieldName);
            }
            final CidsTable table = ((parentFieldName != null)
                    ? new CidsTable(getTableNameFromParentFieldName(parentFieldName)) : null);

            if (isFieldArgument(node, argumentName.equalsIgnoreCase("where"))) {
                if (!operators.contains(fieldName)) {
                    try {
                        if (isMutationArgument(argumentName) && (table != null)) {
                            if (!GraphQlPermissionEvaluator.this.hasWritePermission(table, fieldName)) {
                                return TreeTransformerUtil.deleteNode(context);
                            }
                        }
                        if (table != null) {
                            checkReadPermission(table, fieldName);
                        }
                    } catch (FieldNotFoundException e) {
                        return TreeTransformerUtil.deleteNode(context);
                    } catch (TableNotFoundException e) {
                        return TreeTransformerUtil.deleteNode(context);
                    } catch (RemoteException e) {
                        LOG.error("Remote Exception ", e);
                        return TreeTransformerUtil.deleteNode(context);
                    }
                }
            } else if (isTable(node, argumentName.equalsIgnoreCase("where"))) {
                try {
                    final CidsField foreignField = determineForeignKeyField(table, fieldName);
                    if (isMutationArgument(argumentName) && (foreignField != null)) {
                        if (!GraphQlPermissionEvaluator.this.hasWritePermission(table, fieldName)) {
                            return TreeTransformerUtil.deleteNode(context);
                        }
                    }
                } catch (FieldNotFoundException e) {
                    return TreeTransformerUtil.deleteNode(context);
                } catch (TableNotFoundException e) {
                    return TreeTransformerUtil.deleteNode(context);
                } catch (RemoteException e) {
                    LOG.error("Remote Exception ", e);
                    return TreeTransformerUtil.deleteNode(context);
                }
            }
            return super.visitObjectField(node, context);
        }

        @Override
        public TraversalControl visitOperationDefinition(final OperationDefinition node,
                final TraverserContext<Node> context) {
            operation = node.getOperation();
            return super.visitOperationDefinition(node, context);
        }

        @Override
        public TraversalControl visitField(final Field node, final TraverserContext<Node> context) {
            String fieldName = node.getName();
            final Field parentField = getParentField(context.getParentNodes());
            String parentFieldName = ((parentField != null) ? parentField.getName() : null);

            if ((operation != null) && operation.equals(OperationDefinition.Operation.MUTATION)) {
                if (isSpecialMutationField(fieldName)) {
                    return TraversalControl.CONTINUE;
                }
                fieldName = mutationFieldNameToFieldName(fieldName);
                parentFieldName = mutationFieldNameToFieldName(parentFieldName);
            }

            if ((lastTable == null) || (parentFieldName == null)) {
                lastTable = new CidsTable(fieldName);
                if ((operation != null) && operation.equals(OperationDefinition.Operation.MUTATION)) {
                    try {
                        if (!hasWritePermission(lastTable)) {
                            fieldsWithoutPermission.add(lastTable);
                            return TreeTransformerUtil.deleteNode(context);
                        }
                    } catch (TableNotFoundException e) {
                        fieldsWithoutPermission.add(lastTable);
                        return TreeTransformerUtil.deleteNode(context);
                    } catch (RemoteException e) {
                        LOG.error("remote exception", e);
                        return TreeTransformerUtil.deleteNode(context);
                    }
                }
            } else {
                if (node.getSelectionSet() == null) {
                    CidsTable table = fieldToTable.get(parentFieldName);

                    if (table == null) {
                        table = new CidsTable(getTableNameFromParentFieldName(parentFieldName));
                        try {
                            checkReadPermission(table, fieldName);
                        } catch (Exception e) {
                            final Field gp = getGrandParentField(context.getParentNodes());

                            if (gp != null) {
                                try {
//                                    CidsField f = determineForeignKeyField(gp.getName(), gp.getAlias());
                                    final String tName = determineTableForForeignKeyField(new CidsTable(gp.getName()),
                                            parentField.getAlias());

                                    table = new CidsTable(tName);
                                } catch (Exception ex) {
                                    // nothing to do
                                }
                            }
                        }
                    }
                    try {
                        checkReadPermission(table, fieldName);
                    } catch (FieldNotFoundException e) {
                        fieldsWithoutPermission.add(new CidsField(table, fieldName));
                        return TreeTransformerUtil.deleteNode(context);
                    } catch (TableNotFoundException e) {
                        fieldsWithoutPermission.add(new CidsField(table, fieldName));
                        return TreeTransformerUtil.deleteNode(context);
                    } catch (RemoteException e) {
                        LOG.error("remote exception", e);
                        return TreeTransformerUtil.deleteNode(context);
                    }
                }

                if (node.getSelectionSet() != null) {
                    if (lastTable != null) {
                        CidsTable table = fieldToTable.get(parentFieldName);

                        if (table == null) {
                            table = new CidsTable(getTableNameFromParentFieldName(parentFieldName));
                        }
                        try {
                            final CidsField foreignField = determineForeignKeyField(
                                    table,
                                    fieldName);
                            if (foreignField != null) {
                                lastTable = new CidsTable(determineTableForForeignKeyField(table, fieldName));
                                fieldToTable.put(fieldName, lastTable);
                            }
                        } catch (FieldNotFoundException e) {
                            fieldsWithoutPermission.add(new CidsField(table, fieldName));
                            return TreeTransformerUtil.deleteNode(context);
                        } catch (TableNotFoundException e) {
                            fieldsWithoutPermission.add(new CidsField(table, fieldName));
                            return TreeTransformerUtil.deleteNode(context);
                        } catch (RemoteException e) {
                            LOG.error("remote exception", e);
                            return TreeTransformerUtil.deleteNode(context);
                        }
                    }
                }
            }

            return TraversalControl.CONTINUE;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   parents  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getParentTableName(final List<Node> parents) {
            if (parents != null) {
                for (final Node parent : parents) {
                    String name = null;

                    if (parent instanceof Field) {
                        name = ((Field)parent).getName();
                    } else if (parent instanceof ObjectField) {
                        name = ((ObjectField)parent).getName();
                    }

                    if ((name != null) && !operators.contains(name)) {
                        for (final String extension : FIELD_EXTENSIONS) {
                            if (name.endsWith(extension)) {
                                return null;
                            }
                        }

                        return name;
                    }
                }
            }

            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   fields  DOCUMENT ME!
         * @param   field   DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getParent(final ObjectField fields, final String field) {
            if (fields.getName().equals(field)) {
                return null;
            }

            String lastName = null;
            ObjectField currentField = fields;

            if (!operators.contains(fields.getName())) {
                lastName = fields.getName();
            }

            while (currentField.getValue() != null) {
                if (currentField.getValue() instanceof ObjectValue) {
                    final ObjectValue tmp = (ObjectValue)currentField.getValue();
                    final Node tmpNode = tmp.getNamedChildren()
                                .getChildren("objectFields")
                                .get(tmp.getNamedChildren().getChildren("objectFields").size() - 1);
                    if (tmpNode instanceof ObjectField) {
                        if (((ObjectField)tmpNode).getName().equals(field)) {
                            return lastName;
                        } else {
                            currentField = (ObjectField)tmpNode;
                            if (!operators.contains(((ObjectField)tmpNode).getName())) {
                                lastName = ((ObjectField)tmpNode).getName();
                            }
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }

            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   constraintName  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getTableNameFromParentFieldName(final String constraintName) {
            try {
                MetaClass mc = getMetaClassByName(constraintName);

                if (mc != null) {
                    return mc.getTableName();
                } else {
                    final String[] components = constraintName.split("_");

                    if ((components != null) && (components.length > 1)) {
                        for (int i = 1; i < components.length; ++i) {
                            final StringBuffer tmp = new StringBuffer();

                            for (int c = i; c < components.length; ++c) {
                                if (tmp.length() > 0) {
                                    tmp.append("_");
                                }
                                tmp.append(components[c]);
                            }

                            mc = getMetaClassByName(tmp.toString());

                            if (mc != null) {
                                return mc.getName();
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                LOG.error("RemoteException while determining foreign table", e);
            }

            return constraintName;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   fieldName  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String mutationFieldNameToFieldName(final String fieldName) {
            if (fieldName == null) {
                return null;
            } else {
                if (fieldName.startsWith("update_")) {
                    return fieldName.substring("update_".length());
                }
                if (fieldName.startsWith("delete_")) {
                    return fieldName.substring("delete_".length());
                }
                if (fieldName.startsWith("insert_")) {
                    return fieldName.substring("insert_".length());
                }
            }

            return fieldName;
        }

        /**
         * Check if the given field is a special field.
         *
         * @param   fieldName  the name of the field
         *
         * @return  DOCUMENT ME!
         */
        private boolean isSpecialMutationField(final String fieldName) {
            if (fieldName == null) {
                return false;
            } else {
                if (fieldName.equalsIgnoreCase("affected_rows")) {
                    return true;
                }
                if (fieldName.equalsIgnoreCase("returning")) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Determine the parent field.
         *
         * @param   parents  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private Field getParentField(final List<Node> parents) {
            if (parents != null) {
                for (final Node parent : parents) {
                    if (parent instanceof Field) {
                        return (Field)parent;
                    }
                }
            }

            return null;
        }

        /**
         * Determine the parent field.
         *
         * @param   parents  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private Field getGrandParentField(final List<Node> parents) {
            boolean found = false;

            if (parents != null) {
                for (final Node parent : parents) {
                    if (parent instanceof Field) {
                        if (!found) {
                            found = true;
                        } else {
                            return (Field)parent;
                        }
                    }
                }
            }

            return null;
        }

        /**
         * Determine the parent operation.
         *
         * @param   parents  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private OperationDefinition.Operation getOperation(final List<Node> parents) {
            if (parents != null) {
                for (final Node parent : parents) {
                    if (parent instanceof OperationDefinition) {
                        return ((OperationDefinition)parent).getOperation();
                    }
                }
            }

            return null;
        }

        /**
         * Determine the parent argument.
         *
         * @param   parents  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private Argument getArgument(final List<Node> parents) {
            if (parents != null) {
                for (final Node parent : parents) {
                    if (parent instanceof Argument) {
                        return (Argument)parent;
                    }
                }
            }

            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   argumentName  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private boolean isMutationArgument(final String argumentName) {
            return !readArguments.contains(argumentName);
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public List<CidsDataSource> getFieldsWithoutPermission() {
            return fieldsWithoutPermission;
        }
    }
}
