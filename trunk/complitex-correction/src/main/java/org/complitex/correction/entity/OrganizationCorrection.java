package org.complitex.correction.entity;

import org.complitex.dictionary.entity.Correction;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.11.13 15:41
 */
public class OrganizationCorrection extends Correction {
    @Override
    public String getEntity() {
        return "organization";
    }
}
