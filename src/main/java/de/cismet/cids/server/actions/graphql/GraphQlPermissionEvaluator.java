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
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.SelectionSet;

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
//            for (final Definition d : doc.getDefinitions()) {
//                if (d instanceof OperationDefinition) {
//                    if (((OperationDefinition)d).getOperation().equals(OperationDefinition.Operation.QUERY)) {
//                        final SelectionSet selectionSet = ((OperationDefinition)d).getSelectionSet();
//                        collectUsedTableAndFields(null, selectionSet);
//                    }
//                }
//            }
            final AstTransformer transformer = new AstTransformer();
            final CidsVisitor visitor = new CidsVisitor();
            final Document newDoc = (Document)transformer.transform(doc, visitor);

            return AstPrinter.printAst(newDoc);
        } catch (InvalidSyntaxException e) {
            LOG.error("Error while evaluating graphql query", e);
        }

        return null;
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

                    return !(val.getObjectFields().size() > 0) && (val.getObjectFields().get(0) instanceof ObjectField);
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
                    final MetaClass referencedTable = getMetaClassById(mai.getForeignKeyClassId());

                    if (referencedTable != null) {
                        if (referencedTable.getTableName().equalsIgnoreCase(foreignKeyDestinaton)) {
                            return new CidsField(table, mai.getFieldName());
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
        final MetaClass mc = getMetaClassByName(table.getName());

        if (mc == null) {
            throw new TableNotFoundException(table.getName());
        }

        for (final Object attribute : mc.getMemberAttributeInfos().values()) {
            if (attribute instanceof MemberAttributeInfo) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)attribute;

                if (mai.getFieldName().equalsIgnoreCase(fieldName)) {
                    return;
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
        final MetaClass mc = getMetaClassByName(table.getName());

        if (mc == null) {
            throw new TableNotFoundException(table.getName());
        }

        for (final Object attribute : mc.getMemberAttributeInfos().values()) {
            if (attribute instanceof MemberAttributeInfo) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)attribute;

                if (mai.getFieldName().equalsIgnoreCase(fieldName)) {
                    if (mc.getPermissions().hasWritePermission(user)) {
                        // todo the write permission for the field should be checked too
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
        return ms.getClassByTableName(user, name, cc);
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

        private final List<String> readArguments = new ArrayList<String>();
        private final List<CidsDataSource> fieldsWithoutPermission = new ArrayList<CidsDataSource>();
        private CidsTable lastTable = null;
        private OperationDefinition.Operation operation = null;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ReadVisitor object.
         */
        public CidsVisitor() {
            readArguments.add("where");
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
            final Field parentField = getParentField(context.getParentNodes());
            String parentFieldName = ((parentField != null) ? parentField.getName() : null);

            if (operation.equals(OperationDefinition.Operation.MUTATION)) {
                if (isSpecialMutationField(fieldName)) {
                    return TraversalControl.CONTINUE;
                }
                fieldName = mutationFieldNameToFieldName(fieldName);
                parentFieldName = mutationFieldNameToFieldName(parentFieldName);
            }
            final CidsTable table = new CidsTable(parentFieldName);

            if (isFieldArgument(node, argumentName.equalsIgnoreCase("where"))) {
                try {
                    if (isMutationArgument(argumentName)) {
                        if (!GraphQlPermissionEvaluator.this.hasWritePermission(table, fieldName)) {
                            return TreeTransformerUtil.deleteNode(context);
                        }
                    }
                    checkReadPermission(table, fieldName);
                } catch (FieldNotFoundException e) {
                    return TreeTransformerUtil.deleteNode(context);
                } catch (TableNotFoundException e) {
                    return TreeTransformerUtil.deleteNode(context);
                } catch (RemoteException e) {
                    LOG.error("Remote Exception ", e);
                    return TreeTransformerUtil.deleteNode(context);
                }
            } else if (isTable(node, argumentName.equalsIgnoreCase("where"))) {
                try {
                    final CidsField foreignField = determineForeignKeyField(table, fieldName);
                    if (isMutationArgument(argumentName)) {
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
//            System.out.println("fieldVisit: " + node.getName());
            String fieldName = node.getName();
            final Field parentField = getParentField(context.getParentNodes());
            String parentFieldName = ((parentField != null) ? parentField.getName() : null);

            if (operation.equals(OperationDefinition.Operation.MUTATION)) {
                if (isSpecialMutationField(fieldName)) {
                    return TraversalControl.CONTINUE;
                }
                fieldName = mutationFieldNameToFieldName(fieldName);
                parentFieldName = mutationFieldNameToFieldName(parentFieldName);
            }

            if (lastTable == null) {
                lastTable = new CidsTable(fieldName);
                if (operation.equals(OperationDefinition.Operation.MUTATION)) {
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
                    final CidsTable table = new CidsTable(parentFieldName);
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
                        final CidsTable table = new CidsTable(parentFieldName);
                        try {
                            final CidsField foreignField = determineForeignKeyField(
                                    table,
                                    fieldName);

                            lastTable = new CidsTable(fieldName);
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
