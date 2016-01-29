/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.nodepermissions;

import Sirius.server.middleware.types.MetaObjectNode;

/**
 *
 * @author thorsten
 */
public interface ObjectNodeStore extends ObjectNodeProvider{
    void setObjectNode(MetaObjectNode mon);
}
