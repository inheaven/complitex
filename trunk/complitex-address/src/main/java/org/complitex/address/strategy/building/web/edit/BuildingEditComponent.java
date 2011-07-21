/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.building.web.edit;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.DomainObjectAccessUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.list.AjaxRemovableListView;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Artem
 */
public final class BuildingEditComponent extends AbstractComplexAttributesPanel {

    @EJB
    private DistrictStrategy districtStrategy;
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private StrategyFactory strategyFactory;
    private SearchComponentState districtComponentState;

    private class DistrictSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(Component component, final Map<String, Long> ids, final AjaxRequestTarget target) {
            DomainObject district = districtComponentState.get("district");
            if (district != null && district.getId() > 0) {
                districtAttribute.setValueId(district.getId());
            } else {
                districtAttribute.setValueId(null);
            }
        }
    }

    public BuildingEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }
    private FeedbackPanel messages;

    private FeedbackPanel findFeedbackPanel() {
        if (messages == null) {
            getPage().visitChildren(FeedbackPanel.class, new IVisitor<FeedbackPanel>() {

                @Override
                public Object component(FeedbackPanel feedbackPanel) {
                    messages = feedbackPanel;
                    return STOP_TRAVERSAL;
                }
            });
        }
        return messages;
    }
    private Attribute districtAttribute;

    @Override
    protected void init() {
        final FeedbackPanel feedbackPanel = findFeedbackPanel();
        final WebMarkupContainer attributesContainer = new WebMarkupContainer("attributesContainer");
        attributesContainer.setOutputMarkupId(true);
        add(attributesContainer);

        final Building building = (Building) getInputPanel().getObject();

        final SearchComponentState parentSearchComponentState = getInputPanel().getParentSearchComponentState();

        //district
        WebMarkupContainer districtContainer = new WebMarkupContainer("districtContainer");
        attributesContainer.add(districtContainer);

        Label districtLabel = new Label("districtLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                IStrategy buildingStrategy = strategyFactory.getStrategy("building");
                return stringBean.displayValue(buildingStrategy.getEntity().getAttributeType(BuildingStrategy.DISTRICT).getAttributeNames(), getLocale());
            }
        });
        districtContainer.add(districtLabel);
        districtComponentState = new SearchComponentState() {

            //todo whether there is a need to override?
            @Override
            public DomainObject put(String entity, DomainObject object) {
                super.put(entity, object);

                if ("district".equals(entity)) {
                    building.setDistrict(object);
                }

                return object;
            }
        };
        districtComponentState.updateState(parentSearchComponentState);


        districtAttribute = building.getAttribute(BuildingStrategy.DISTRICT);

        if (districtAttribute != null) {
            Long districtId = districtAttribute.getValueId();

            if (districtId != null) {
                DomainObject district = districtStrategy.findById(districtId, true);
                districtComponentState.put("district", district);
            }
        }

        districtContainer.add(new WiQuerySearchComponent("district", districtComponentState,
                ImmutableList.of("country", "region", "city", "district"), new DistrictSearchCallback(), ShowMode.ACTIVE,
                !isDisabled() && DomainObjectAccessUtil.canEdit(null, "building", building)));
        districtContainer.setVisible(districtAttribute != null);

        //primary building address
        final DomainObject primaryBuildingAddress = building.getPrimaryAddress();
        DomainObjectInputPanel primaryAddressPanel = new DomainObjectInputPanel("primaryAddress", primaryBuildingAddress,
                "building_address", null, getInputPanel().getParentId(), getInputPanel().getParentEntity(), getInputPanel().getDate()) {

            @Override
            public SearchComponentState initParentSearchComponentState() {
                SearchComponentState primaryAddressComponentState = super.initParentSearchComponentState();

                if (primaryBuildingAddress.getId() == null) {
                    primaryAddressComponentState.updateState(parentSearchComponentState);
                }
                return primaryAddressComponentState;
            }
        };
        attributesContainer.add(primaryAddressPanel);

        //alternative addresses
        ListView<DomainObject> alternativeAdresses = new AjaxRemovableListView<DomainObject>("alternativeAdresses",
                building.getAlternativeAddresses()) {

            @Override
            protected void populateItem(ListItem<DomainObject> item) {
                final DomainObject address = item.getModelObject();

                DomainObjectInputPanel alternativeAddess = new DomainObjectInputPanel("alternativeAddess", address,
                        "building_address", null, getInputPanel().getParentId(), getInputPanel().getParentEntity(),
                        getInputPanel().getDate()) {

                    @Override
                    public SearchComponentState initParentSearchComponentState() {
                        SearchComponentState alternativeAddressComponentState = null;
                        if (address.getId() == null) {
                            alternativeAddressComponentState = new SearchComponentState();
                            alternativeAddressComponentState.updateState(parentSearchComponentState);
                            alternativeAddressComponentState.put("street", null);
                        } else {
                            alternativeAddressComponentState = super.initParentSearchComponentState();
                        }
                        return alternativeAddressComponentState;
                    }
                };
                item.add(alternativeAddess);
                addRemoveSubmitLink("remove", findParent(Form.class), item, null, attributesContainer, feedbackPanel).
                        setVisible(!isDisabled() && DomainObjectAccessUtil.canEdit(null, "building", building));
            }
        };
        attributesContainer.add(alternativeAdresses);

        AjaxSubmitLink add = new AjaxSubmitLink("add") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                IStrategy buildingAddressStrategy = strategyFactory.getStrategy("building_address");
                DomainObject newBuildingAddress = buildingAddressStrategy.newInstance();
                building.addAlternativeAddress(newBuildingAddress);

                target.addComponent(attributesContainer);
                target.addComponent(feedbackPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(feedbackPanel);
            }
        };
        add.setVisible(!isDisabled() && DomainObjectAccessUtil.canEdit(null, "building", building));
        add(add);
    }
}
