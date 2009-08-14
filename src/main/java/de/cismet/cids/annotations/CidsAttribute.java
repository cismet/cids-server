/*
 * CidsAttribute.java
 *
 * Created on 24. Mai 2007, 14:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cids.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author hell
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface CidsAttribute {
        String value();
}