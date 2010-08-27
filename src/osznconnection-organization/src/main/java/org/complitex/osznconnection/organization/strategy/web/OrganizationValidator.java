/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy.web;

import org.apache.wicket.Component;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionaryfw.strategy.web.IValidator;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

/**
 *
 * @author Artem
 */
public class OrganizationValidator implements IValidator {

    private OrganizationEditComponent organizationEditComponent;

    private OrganizationStrategy organizationStrategy;

    public OrganizationValidator(OrganizationStrategy organizationStrategy) {
        this.organizationStrategy = organizationStrategy;
    }

    @Override
    public boolean validate(DomainObject object, Component component) {
        boolean valid = checkParent(object, getParentEditComponent((DomainObjectEditPanel) component));
        valid &= checkDistrictCode(object, component);
        return valid;
    }

    private OrganizationEditComponent getParentEditComponent(DomainObjectEditPanel editPanel) {
        if (organizationEditComponent == null) {
            editPanel.visitChildren(OrganizationEditComponent.class, new Component.IVisitor<OrganizationEditComponent>() {

                @Override
                public Object component(OrganizationEditComponent component) {
                    organizationEditComponent = component;
                    return STOP_TRAVERSAL;
                }
            });
        }
        return organizationEditComponent;
    }

    private boolean checkParent(DomainObject object, OrganizationEditComponent component) {
        long entityTypeId = object.getEntityTypeId();
        if ((entityTypeId == OrganizationStrategy.OSZN) && component.getParentObject() != null) {
            component.getPage().error(ResourceUtil.getString(OrganizationStrategy.RESOURCE_BUNDLE, "oszn_cannot_have_parent", component.getLocale()));
            return false;
        }
        if ((entityTypeId == OrganizationStrategy.PU) && (component.getParentObject() == null)) {
            component.getPage().error(ResourceUtil.getString(OrganizationStrategy.RESOURCE_BUNDLE, "pu_must_have_parent", component.getLocale()));
            return false;
        }
        return true;
    }

    private boolean checkDistrictCode(DomainObject object, Component component) {
        long entityTypeId = object.getEntityTypeId();
        String districtCode = organizationStrategy.getDistrictCode(object);
        if ((entityTypeId == OrganizationStrategy.OSZN) && Strings.isEmpty(districtCode)) {
            component.getPage().error(ResourceUtil.getString(OrganizationStrategy.RESOURCE_BUNDLE, "oszn_must_have_district_code", component.getLocale()));
            return false;
        }
        if ((entityTypeId == OrganizationStrategy.PU) && !Strings.isEmpty(districtCode)) {
            component.getPage().error(ResourceUtil.getString(OrganizationStrategy.RESOURCE_BUNDLE, "pu_cant_have_district_code", component.getLocale()));
            return false;
        }
        return true;
    }
}
