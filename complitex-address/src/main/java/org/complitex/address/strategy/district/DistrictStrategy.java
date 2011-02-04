package org.complitex.address.strategy.district;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.web.DomainObjectListPanel;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.address.resource.CommonResources;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.template.strategy.AbstractStrategy;

/**
 *
 * @author Artem
 */
@Stateless(name = "DistrictStrategy")
public class DistrictStrategy extends AbstractStrategy {

    private static final String DISTRICT_NAMESPACE = DistrictStrategy.class.getPackage().getName() + ".District";
    @EJB(beanName = "StringCultureBean")
    private StringCultureBean stringBean;

    /*
     * Attribute type ids
     */
    private static final long NAME = 600;
    private static final long CODE = 601;

    @Override
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(NAME);
    }

    @Override
    public String getEntityTable() {
        return "district";
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return stringBean.displayValue(object.getAttribute(NAME).getLocalizedValues(), locale);
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return new SearchCallback();
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        configureExampleImpl(example, ids, searchTextInput);
    }

    private static void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = example.getAttributeExample(NAME);
            if (attrExample == null) {
                attrExample = new AttributeExample(NAME);
                example.addAttributeExample(attrExample);
            }
            attrExample.setValue(searchTextInput);
        }
        Long cityId = ids.get("city");
        example.setParentId(cityId);
        example.setParentEntity("city");
    }

    @Override
    public List<String> getSearchFilters() {
        return ImmutableList.of("country", "region", "city");
    }

    private static class SearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectListPanel list = component.findParent(DomainObjectListPanel.class);
            configureExampleImpl(list.getExample(), ids, null);
            list.refreshContent(target);
        }
    }

    @Override
    public ISearchCallback getParentSearchCallback() {
        return new ParentSearchCallback();
    }

    private static class ParentSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectInputPanel inputPanel = component.findParent(DomainObjectInputPanel.class);
            Long cityId = ids.get("city");
            if (cityId != null && cityId > 0) {
                inputPanel.getObject().setParentId(cityId);
                inputPanel.getObject().setParentEntityId(400L);
            } else {
                inputPanel.getObject().setParentId(null);
                inputPanel.getObject().setParentEntityId(null);
            }
        }
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public String[] getChildrenEntities() {
        return new String[]{"street"};
    }

    @Override
    public String[] getParents() {
        return new String[]{"city"};
    }

    public String getDistrictCode(long districtId) {
        DomainObject district = findById(districtId);
        return stringBean.getSystemStringCulture(district.getAttribute(CODE).getLocalizedValues()).getValue();
    }

    @Transactional
    @Override
    public List<? extends DomainObjectPermissionInfo> findChildren(long parentId, String childEntity, int start, int size) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("parentId", parentId);
        params.put("start", start);
        params.put("size", size);
        return sqlSession().selectList(DISTRICT_NAMESPACE + "." + FIND_CHILDREN_OPERATION, params);
    }
}
