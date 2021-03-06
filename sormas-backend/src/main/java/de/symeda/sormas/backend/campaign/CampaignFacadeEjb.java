package de.symeda.sormas.backend.campaign;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;

import de.symeda.sormas.api.campaign.CampaignCriteria;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignFacade;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.diagram.CampaignDashboardElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.campaign.form.CampaignFormMetaFacadeEjb;
import de.symeda.sormas.backend.campaign.form.CampaignFormMetaService;
import de.symeda.sormas.backend.common.AbstractAdoService;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.user.UserRoleConfigFacadeEjb.UserRoleConfigFacadeEjbLocal;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "CampaignFacade")
public class CampaignFacadeEjb implements CampaignFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private CampaignService campaignService;
	@EJB
	private CampaignFormMetaService campaignFormMetaService;
	@EJB
	private UserService userService;
	@EJB
	private UserRoleConfigFacadeEjbLocal userRoleConfigFacade;
	@EJB
	private CampaignFacadeEjbLocal campaignFacade;
	@EJB
	private CampaignFormMetaFacadeEjb.CampaignFormMetaFacadeEjbLocal campaignFormMetaFacade;

	@Override
	public List<CampaignIndexDto> getIndexList(CampaignCriteria campaignCriteria, Integer first, Integer max, List<SortProperty> sortProperties) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignIndexDto> cq = cb.createQuery(CampaignIndexDto.class);
		Root<Campaign> campaign = cq.from(Campaign.class);

		cq.multiselect(campaign.get(Campaign.UUID), campaign.get(Campaign.NAME), campaign.get(Campaign.START_DATE), campaign.get(Campaign.END_DATE));

		Predicate filter = campaignService.createUserFilter(cb, cq, campaign);

		if (campaignCriteria != null) {
			Predicate criteriaFilter = campaignService.buildCriteriaFilter(campaignCriteria, cb, campaign);
			filter = AbstractAdoService.and(cb, filter, criteriaFilter);
		}

		cq.where(filter);

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<Order>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case CampaignIndexDto.UUID:
				case CampaignIndexDto.NAME:
				case CampaignIndexDto.START_DATE:
				case CampaignIndexDto.END_DATE:
					expression = campaign.get(sortProperty.propertyName);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(campaign.get(Campaign.CHANGE_DATE)));
		}

		if (first != null && max != null) {
			return em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
		} else {
			return em.createQuery(cq).getResultList();
		}
	}

	@Override
	public List<CampaignReferenceDto> getAllActiveCampaignsAsReference() {
		return campaignService.getAll()
			.stream()
			.filter(c -> !c.isDeleted() && !c.isArchived())
			.map(CampaignFacadeEjb::toReferenceDto)
			.collect(Collectors.toList());
	}

	@Override
	public CampaignReferenceDto getLastStartedCampaign() {

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Campaign> query = cb.createQuery(Campaign.class);
		final Root<Campaign> from = query.from(Campaign.class);
		query.select(from);
		query.where(cb.lessThanOrEqualTo(from.get(Campaign.START_DATE), new Date()));
		query.orderBy(cb.desc(from.get(Campaign.START_DATE)));

		final TypedQuery<Campaign> q = em.createQuery(query);
		final Campaign lastStartedCampaign = q.getResultList().stream().findFirst().orElse(null);

		return toReferenceDto(lastStartedCampaign);
	}

	@Override
	public long count(CampaignCriteria campaignCriteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Campaign> campaign = cq.from(Campaign.class);

		Predicate filter = campaignService.createUserFilter(cb, cq, campaign);

		if (campaignCriteria != null) {
			Predicate criteriaFilter = campaignService.buildCriteriaFilter(campaignCriteria, cb, campaign);
			filter = AbstractAdoService.and(cb, filter, criteriaFilter);
		}

		cq.where(filter);
		cq.select(cb.count(campaign));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public CampaignDto saveCampaign(CampaignDto dto) {

		Campaign campaign = fromDto(dto);
		campaignService.ensurePersisted(campaign);
		return toDto(campaign);
	}

	public Campaign fromDto(@NotNull CampaignDto source) {

		Campaign target = campaignService.getByUuid(source.getUuid());
		if (target == null) {
			target = new Campaign();
			target.setUuid(source.getUuid());
			if (source.getCreationDate() != null) {
				target.setCreationDate(new Timestamp(source.getCreationDate().getTime()));
			}
		}
		DtoHelper.validateDto(source, target);

		validate(source);

		target.setCreatingUser(userService.getByReferenceDto(source.getCreatingUser()));
		target.setDescription(source.getDescription());
		target.setEndDate(source.getEndDate());
		target.setName(source.getName());
		target.setStartDate(source.getStartDate());
		final Set<CampaignFormMetaReferenceDto> campaignFormMetas = source.getCampaignFormMetas();
		if (!CollectionUtils.isEmpty(campaignFormMetas)) {
			target.setCampaignFormMetas(
				campaignFormMetas.stream()
					.map(campaignFormMetaReferenceDto -> campaignFormMetaService.getByUuid(campaignFormMetaReferenceDto.getUuid()))
					.collect(Collectors.toSet()));
		}
		target.setDashboardElements(source.getCampaignDashboardElements());

		return target;
	}

	private void validate(CampaignDto campaignDto) {
		final List<CampaignDashboardElement> campaignDashboardElements = campaignDto.getCampaignDashboardElements();
		if (campaignDashboardElements != null) {
			for (CampaignDashboardElement cde : campaignDashboardElements) {
				if (cde.getDiagramId() == null
					|| cde.getTabId() == null
					|| cde.getWidth() == null
					|| cde.getHeight() == null
					|| cde.getOrder() == null) {
					throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.campaignDashboardChartValueNull));
				}
			}
		}
	}

	public CampaignDto toDto(Campaign source) {

		if (source == null) {
			return null;
		}

		CampaignDto target = new CampaignDto();
		DtoHelper.fillDto(target, source);

		target.setCreatingUser(UserFacadeEjb.toReferenceDto(source.getCreatingUser()));
		target.setDescription(source.getDescription());
		target.setEndDate(source.getEndDate());
		target.setName(source.getName());
		target.setStartDate(source.getStartDate());
		target.setCampaignFormMetas(
			source.getCampaignFormMetas().stream().map(campaignFormMeta -> campaignFormMeta.toReference()).collect(Collectors.toSet()));
		target.setCampaignDashboardElements(source.getDashboardElements());

		return target;
	}

	@Override
	public CampaignDto getByUuid(String uuid) {
		return toDto(campaignService.getByUuid(uuid));
	}

	@Override
	public List<CampaignDashboardElement> getCampaignDashboardElements(String campaignUuid) {
		final List<CampaignDashboardElement> result = new ArrayList<>();
		if (campaignUuid != null) {
			final Campaign campaign = campaignService.getByUuid(campaignUuid);
			validate(toDto(campaign));
			List<CampaignDashboardElement> dashboardElements = campaign.getDashboardElements();
			if (dashboardElements != null) {
				result.addAll(dashboardElements);
			}
		} else {
			campaignService.getAllActive().forEach(campaign -> {
				validate(toDto(campaign));
				if (campaign.getDashboardElements() != null) {
					result.addAll(campaign.getDashboardElements());
				}
			});
		}
		return result.stream().sorted(Comparator.comparingInt(CampaignDashboardElement::getOrder)).collect(Collectors.toList());
	}

	@Override
	public boolean isArchived(String uuid) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Campaign> from = cq.from(Campaign.class);

		// Workaround for probable bug in Eclipse Link/Postgre that throws a
		// NoResultException when trying to
		// query for a true Boolean result
		cq.where(cb.and(cb.equal(from.get(Campaign.ARCHIVED), true), cb.equal(from.get(AbstractDomainObject.UUID), uuid)));
		cq.select(cb.count(from));
		long count = em.createQuery(cq).getSingleResult();
		return count > 0;
	}

	@Override
	public void deleteCampaign(String campaignUuid) {

		User user = userService.getCurrentUser();
		if (!userRoleConfigFacade.getEffectiveUserRights(user.getUserRoles().toArray(new UserRole[user.getUserRoles().size()]))
			.contains(UserRight.CAMPAIGN_DELETE)) {
			throw new UnsupportedOperationException(
				I18nProperties.getString(Strings.entityUser) + " " + user.getUuid() + " is not allowed to delete "
					+ I18nProperties.getString(Strings.entityCampaigns).toLowerCase() + ".");
		}

		campaignService.delete(campaignService.getByUuid(campaignUuid));
	}

	@Override
	public void archiveOrDearchiveCampaign(String campaignUuid, boolean archive) {

		Campaign campaign = campaignService.getByUuid(campaignUuid);
		campaign.setArchived(archive);
		campaignService.ensurePersisted(campaign);
	}

	@Override
	public CampaignReferenceDto getReferenceByUuid(String uuid) {
		return toReferenceDto(campaignService.getByUuid(uuid));
	}

	@Override
	public boolean exists(String uuid) {
		return campaignService.exists(uuid);
	}

	@Override
	public List<CampaignDto> getAllAfter(Date date) {
		return campaignService.getAllAfter(date, userService.getCurrentUser())
			.stream()
			.map(campaignFormMeta -> toDto(campaignFormMeta))
			.collect(Collectors.toList());
	}

	@Override
	public List<CampaignDto> getByUuids(List<String> uuids) {
		return campaignService.getByUuids(uuids).stream().map(c -> toDto(c)).collect(Collectors.toList());
	}

	@Override
	public List<String> getAllActiveUuids() {
		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}

		return campaignService.getAllActiveUuids();
	}

	public static CampaignReferenceDto toReferenceDto(Campaign entity) {
		if (entity == null) {
			return null;
		}
		CampaignReferenceDto dto = new CampaignReferenceDto(entity.getUuid(), entity.toString());
		return dto;
	}

	@LocalBean
	@Stateless
	public static class CampaignFacadeEjbLocal extends CampaignFacadeEjb {
	}
}
