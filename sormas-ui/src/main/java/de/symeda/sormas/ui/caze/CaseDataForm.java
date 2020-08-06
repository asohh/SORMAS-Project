/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.caze;

import static de.symeda.sormas.ui.utils.CssStyles.ERROR_COLOR_PRIMARY;
import static de.symeda.sormas.ui.utils.CssStyles.FORCE_CAPTION;
import static de.symeda.sormas.ui.utils.CssStyles.H3;
import static de.symeda.sormas.ui.utils.CssStyles.LAYOUT_COL_HIDE_INVSIBLE;
import static de.symeda.sormas.ui.utils.CssStyles.SOFT_REQUIRED;
import static de.symeda.sormas.ui.utils.CssStyles.VSPACE_3;
import static de.symeda.sormas.ui.utils.CssStyles.style;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumn;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumnLoc;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumnLocCss;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRow;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRowLocs;
import static de.symeda.sormas.ui.utils.LayoutUtil.inlineLocs;
import static de.symeda.sormas.ui.utils.LayoutUtil.loc;
import static de.symeda.sormas.ui.utils.LayoutUtil.locCss;
import static de.symeda.sormas.ui.utils.LayoutUtil.locs;

import java.util.Arrays;
import java.util.List;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.converter.Converter.ConversionException;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.DateField;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseLogic;
import de.symeda.sormas.api.caze.CaseOrigin;
import de.symeda.sormas.api.caze.CaseOutcome;
import de.symeda.sormas.api.caze.HospitalWardType;
import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.api.caze.Vaccination;
import de.symeda.sormas.api.caze.classification.DiseaseClassificationCriteriaDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.contact.QuarantineType;
import de.symeda.sormas.api.event.TypeOfPlace;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.facility.FacilityTypeGroup;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.api.utils.fieldaccess.FieldAccessCheckers;
import de.symeda.sormas.api.utils.fieldvisibility.FieldVisibilityCheckers;
import de.symeda.sormas.api.utils.fieldvisibility.checkers.CountryFieldVisibilityChecker;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.utils.AbstractEditForm;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent.DoneListener;
import de.symeda.sormas.ui.utils.ConfirmationComponent;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.FieldHelper;
import de.symeda.sormas.ui.utils.OutbreakFieldVisibilityChecker;
import de.symeda.sormas.ui.utils.StringToAngularLocationConverter;
import de.symeda.sormas.ui.utils.VaadinUiUtil;
import de.symeda.sormas.ui.utils.ViewMode;

public class CaseDataForm extends AbstractEditForm<CaseDataDto> {

	private static final long serialVersionUID = 1L;

	private static final String CASE_DATA_HEADING_LOC = "caseDataHeadingLoc";
	private static final String MEDICAL_INFORMATION_LOC = "medicalInformationLoc";
	private static final String PAPER_FORM_DATES_LOC = "paperFormDatesLoc";
	private static final String SMALLPOX_VACCINATION_SCAR_IMG = "smallpoxVaccinationScarImg";
	private static final String CLASSIFICATION_RULES_LOC = "classificationRulesLoc";
	private static final String CLASSIFIED_BY_SYSTEM_LOC = "classifiedBySystemLoc";
	private static final String ASSIGN_NEW_EPID_NUMBER_LOC = "assignNewEpidNumberLoc";
	private static final String EPID_NUMBER_WARNING_LOC = "epidNumberWarningLoc";
	private static final String GENERAL_COMMENT_LOC = "generalCommentLoc";
	private static final String FACILITY_OR_HOME_LOC = "facilityOrHomeLoc";
	private static final String TYPE_GROUP_LOC = "typeGroupLoc";

	//@formatter:off
	private static final String HTML_LAYOUT =
			loc(CASE_DATA_HEADING_LOC) +
					fluidRowLocs(4, CaseDataDto.UUID, 3, CaseDataDto.REPORT_DATE, 5, CaseDataDto.REPORTING_USER) +
					fluidRowLocs(4, CaseDataDto.CLINICAL_CONFIRMATION, 4, CaseDataDto.EPIDEMIOLOGICAL_CONFIRMATION, 4, CaseDataDto.LABORATORY_DIAGNOSTIC_CONFIRMATION) +
					inlineLocs(CaseDataDto.CASE_CLASSIFICATION, CLASSIFICATION_RULES_LOC) +
					fluidRow(
							fluidColumnLoc(3, 0, CaseDataDto.CLASSIFICATION_DATE),
							fluidColumnLocCss(LAYOUT_COL_HIDE_INVSIBLE, 5, 0, CaseDataDto.CLASSIFICATION_USER),
							fluidColumnLocCss(LAYOUT_COL_HIDE_INVSIBLE, 4, 0, CLASSIFIED_BY_SYSTEM_LOC)) +
					fluidRowLocs(9, CaseDataDto.INVESTIGATION_STATUS, 3, CaseDataDto.INVESTIGATED_DATE) +
					fluidRowLocs(6, CaseDataDto.EPID_NUMBER, 3, ASSIGN_NEW_EPID_NUMBER_LOC) +
					loc(EPID_NUMBER_WARNING_LOC) +
					fluidRowLocs(6, CaseDataDto.EXTERNAL_ID, 6, null) +
					fluidRow(
							fluidColumnLoc(6, 0, CaseDataDto.DISEASE),
							fluidColumn(6, 0, locs(
									CaseDataDto.DISEASE_DETAILS,
									CaseDataDto.PLAGUE_TYPE,
									CaseDataDto.DENGUE_FEVER_TYPE,
									CaseDataDto.RABIES_TYPE))) +
					fluidRowLocs(9, CaseDataDto.OUTCOME, 3, CaseDataDto.OUTCOME_DATE) +
					fluidRowLocs(3, CaseDataDto.SEQUELAE, 9, CaseDataDto.SEQUELAE_DETAILS) +
					fluidRowLocs(CaseDataDto.REPORTING_TYPE,
							"")
					+
					fluidRowLocs(CaseDataDto.CASE_ORIGIN, "") +
					fluidRowLocs(CaseDataDto.REGION, CaseDataDto.DISTRICT, CaseDataDto.COMMUNITY) +
					fluidRowLocs(FACILITY_OR_HOME_LOC, TYPE_GROUP_LOC, CaseDataDto.FACILITY_TYPE) +
					fluidRowLocs(CaseDataDto.HEALTH_FACILITY, CaseDataDto.HEALTH_FACILITY_DETAILS) +
					fluidRowLocs(CaseDataDto.POINT_OF_ENTRY, CaseDataDto.POINT_OF_ENTRY_DETAILS) +
					locCss(VSPACE_3, CaseDataDto.SHARED_TO_COUNTRY) +
					fluidRowLocs(4, CaseDataDto.QUARANTINE_HOME_POSSIBLE, 8, CaseDataDto.QUARANTINE_HOME_POSSIBLE_COMMENT) +
					fluidRowLocs(4, CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED, 8, CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED_COMMENT) +
					fluidRowLocs(6, CaseDataDto.QUARANTINE, 3, CaseDataDto.QUARANTINE_FROM, 3, CaseDataDto.QUARANTINE_TO) +
					fluidRowLocs(CaseDataDto.QUARANTINE_TYPE_DETAILS) +
					fluidRowLocs(CaseDataDto.QUARANTINE_ORDERED_VERBALLY, CaseDataDto.QUARANTINE_ORDERED_VERBALLY_DATE) +
					fluidRowLocs(CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT, CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT_DATE) +
					fluidRowLocs(CaseDataDto.QUARANTINE_HELP_NEEDED) +
					fluidRowLocs(CaseDataDto.REPORT_LAT, CaseDataDto.REPORT_LON, CaseDataDto.REPORT_LAT_LON_ACCURACY) +
					loc(MEDICAL_INFORMATION_LOC) +
					fluidRowLocs(CaseDataDto.PREGNANT, CaseDataDto.POSTPARTUM) + fluidRowLocs(CaseDataDto.TRIMESTER, "")
					+
					fluidRowLocs(CaseDataDto.VACCINATION, CaseDataDto.VACCINATION_DOSES) +
					fluidRowLocs(CaseDataDto.VACCINE, "") +
					fluidRowLocs(CaseDataDto.SMALLPOX_VACCINATION_RECEIVED, CaseDataDto.SMALLPOX_VACCINATION_SCAR) +
					fluidRowLocs(SMALLPOX_VACCINATION_SCAR_IMG) +
					fluidRowLocs(CaseDataDto.VACCINATION_DATE, CaseDataDto.VACCINATION_INFO_SOURCE) +
					fluidRowLocs(CaseDataDto.SURVEILLANCE_OFFICER, CaseDataDto.CLINICIAN_NAME) +
					fluidRowLocs(CaseDataDto.NOTIFYING_CLINIC, CaseDataDto.NOTIFYING_CLINIC_DETAILS) +
					fluidRowLocs(CaseDataDto.CLINICIAN_PHONE, CaseDataDto.CLINICIAN_EMAIL) +
					loc(PAPER_FORM_DATES_LOC) +
					fluidRowLocs(CaseDataDto.DISTRICT_LEVEL_DATE, CaseDataDto.REGION_LEVEL_DATE,
							CaseDataDto.NATIONAL_LEVEL_DATE)
					+ loc(GENERAL_COMMENT_LOC) + fluidRowLocs(CaseDataDto.ADDITIONAL_DETAILS);
	//@formatter:on

	private final String caseUuid;
	private final PersonDto person;
	private final Disease disease;
	private Field<?> quarantine;
	private DateField quarantineFrom;
	private DateField quarantineTo;
	private CheckBox quarantineOrderedVerbally;
	private CheckBox quarantineOrderedOfficialDocument;
	private OptionGroup facilityOrHome;
	private ComboBox facilityTypeGroup;
	private ComboBox facilityType;

	public CaseDataForm(String caseUuid, PersonDto person, Disease disease, ViewMode viewMode, boolean isInJurisdiction) {

		super(
			CaseDataDto.class,
			CaseDataDto.I18N_PREFIX,
			false,
			FieldVisibilityCheckers.withDisease(disease)
				.add(new OutbreakFieldVisibilityChecker(viewMode))
				.add(new CountryFieldVisibilityChecker(FacadeProvider.getConfigFacade().getCountryLocale())),
			FieldAccessCheckers.withPersonalData(r -> UserProvider.getCurrent().hasUserRight(r), isInJurisdiction));

		this.caseUuid = caseUuid;
		this.person = person;
		this.disease = disease;

		addFields();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void addFields() {

		if (person == null || disease == null) {
			return;
		}

		Label caseDataHeadingLabel = new Label(I18nProperties.getString(Strings.headingCaseData));
		caseDataHeadingLabel.addStyleName(H3);
		getContent().addComponent(caseDataHeadingLabel, CASE_DATA_HEADING_LOC);

		// Add fields
		addFields(
			CaseDataDto.UUID,
			CaseDataDto.REPORT_DATE,
			CaseDataDto.REPORTING_USER,
			CaseDataDto.DISTRICT_LEVEL_DATE,
			CaseDataDto.REGION_LEVEL_DATE,
			CaseDataDto.NATIONAL_LEVEL_DATE,
			CaseDataDto.CLASSIFICATION_DATE,
			CaseDataDto.CLASSIFICATION_USER,
			CaseDataDto.CLASSIFICATION_COMMENT,
			CaseDataDto.NOTIFYING_CLINIC,
			CaseDataDto.NOTIFYING_CLINIC_DETAILS,
			CaseDataDto.CLINICIAN_NAME,
			CaseDataDto.CLINICIAN_PHONE,
			CaseDataDto.CLINICIAN_EMAIL);

		TextField epidField = addField(CaseDataDto.EPID_NUMBER, TextField.class);
		epidField.setInvalidCommitted(true);
		epidField.setMaxLength(24);
		style(epidField, ERROR_COLOR_PRIMARY);

		// Button to automatically assign a new epid number
		Button assignNewEpidNumberButton = ButtonHelper.createButton(Captions.actionAssignNewEpidNumber, e -> {
			epidField.setValue(FacadeProvider.getCaseFacade().generateEpidNumber(getValue().toReference()));
		}, ValoTheme.BUTTON_DANGER, FORCE_CAPTION);

		getContent().addComponent(assignNewEpidNumberButton, ASSIGN_NEW_EPID_NUMBER_LOC);
		assignNewEpidNumberButton.setVisible(false);

		Label epidNumberWarningLabel = new Label(I18nProperties.getString(Strings.messageEpidNumberWarning));
		epidNumberWarningLabel.addStyleName(VSPACE_3);
		addField(CaseDataDto.EXTERNAL_ID, TextField.class);

		addField(CaseDataDto.INVESTIGATION_STATUS, OptionGroup.class);
		addField(CaseDataDto.OUTCOME, OptionGroup.class);
		addField(CaseDataDto.SEQUELAE, OptionGroup.class);
		if (isGermanServer()) {
			addField(CaseDataDto.REPORTING_TYPE);
		}
		addFields(CaseDataDto.INVESTIGATED_DATE, CaseDataDto.OUTCOME_DATE, CaseDataDto.SEQUELAE_DETAILS);

		ComboBox diseaseField = addDiseaseField(CaseDataDto.DISEASE, false);
		addField(CaseDataDto.DISEASE_DETAILS, TextField.class);
		addField(CaseDataDto.PLAGUE_TYPE, OptionGroup.class);
		addField(CaseDataDto.DENGUE_FEVER_TYPE, OptionGroup.class);
		addField(CaseDataDto.RABIES_TYPE, OptionGroup.class);

		addField(CaseDataDto.CASE_ORIGIN, TextField.class);

		quarantine = addField(CaseDataDto.QUARANTINE);
		quarantine.addValueChangeListener(e -> updateQuarantineFields());
		quarantineFrom = addField(CaseDataDto.QUARANTINE_FROM, DateField.class);
		quarantineTo = addDateField(CaseDataDto.QUARANTINE_TO, DateField.class, -1);

		if (isGermanServer()) {
			final ComboBox cbCaseClassification = addField(CaseDataDto.CASE_CLASSIFICATION, ComboBox.class);
			cbCaseClassification.addValidator(
				new GermanCaseClassificationValidator(caseUuid, I18nProperties.getValidationError(Validations.caseClassificationInvalid)));

			addField(CaseDataDto.CLINICAL_CONFIRMATION, ComboBox.class);
			addField(CaseDataDto.EPIDEMIOLOGICAL_CONFIRMATION, ComboBox.class);
			addField(CaseDataDto.LABORATORY_DIAGNOSTIC_CONFIRMATION, ComboBox.class);
			quarantineOrderedVerbally = addField(CaseDataDto.QUARANTINE_ORDERED_VERBALLY, CheckBox.class);
			CssStyles.style(quarantineOrderedVerbally, CssStyles.FORCE_CAPTION);
			addField(CaseDataDto.QUARANTINE_ORDERED_VERBALLY_DATE, DateField.class);
			quarantineOrderedOfficialDocument = addField(CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT, CheckBox.class);
			CssStyles.style(quarantineOrderedOfficialDocument, CssStyles.FORCE_CAPTION);
			addField(CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT_DATE, DateField.class);
			setVisible(
				false,
				ContactDto.QUARANTINE_ORDERED_VERBALLY,
				ContactDto.QUARANTINE_ORDERED_VERBALLY_DATE,
				ContactDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT,
				ContactDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT_DATE);
		} else {
			final OptionGroup caseClassificationGroup = addField(CaseDataDto.CASE_CLASSIFICATION, OptionGroup.class);
			caseClassificationGroup.removeItem(CaseClassification.CONFIRMED_NO_SYMPTOMS);
			caseClassificationGroup.removeItem(CaseClassification.CONFIRMED_UNKNOWN_SYMPTOMS);
		}
		TextField quarantineHelpNeeded = addField(CaseDataDto.QUARANTINE_HELP_NEEDED, TextField.class);
		quarantineHelpNeeded.setInputPrompt(I18nProperties.getString(Strings.pleaseSpecify));
		TextField quarantineTypeDetails = addField(CaseDataDto.QUARANTINE_TYPE_DETAILS, TextField.class);
		quarantineTypeDetails.setInputPrompt(I18nProperties.getString(Strings.pleaseSpecify));
		setVisible(
			false,
			CaseDataDto.QUARANTINE_FROM,
			CaseDataDto.QUARANTINE_TO,
			CaseDataDto.QUARANTINE_HELP_NEEDED,
			CaseDataDto.QUARANTINE_TYPE_DETAILS);

		addField(CaseDataDto.QUARANTINE_HOME_POSSIBLE, OptionGroup.class);
		addField(CaseDataDto.QUARANTINE_HOME_POSSIBLE_COMMENT, TextField.class);
		addField(CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED, OptionGroup.class);
		addField(CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED_COMMENT, TextField.class);

		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			CaseDataDto.QUARANTINE_HOME_POSSIBLE_COMMENT,
			CaseDataDto.QUARANTINE_HOME_POSSIBLE,
			Arrays.asList(YesNoUnknown.NO),
			true);
		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED,
			CaseDataDto.QUARANTINE_HOME_POSSIBLE,
			Arrays.asList(YesNoUnknown.YES),
			true);
		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED_COMMENT,
			CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED,
			Arrays.asList(YesNoUnknown.NO),
			true);
		FieldHelper
			.setVisibleWhen(getFieldGroup(), CaseDataDto.QUARANTINE_TYPE_DETAILS, CaseDataDto.QUARANTINE, Arrays.asList(QuarantineType.OTHER), true);
		if (isGermanServer()) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.QUARANTINE_ORDERED_VERBALLY_DATE,
				CaseDataDto.QUARANTINE_ORDERED_VERBALLY,
				Arrays.asList(Boolean.TRUE),
				true);
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT_DATE,
				CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT,
				Arrays.asList(Boolean.TRUE),
				true);
		}

		ComboBox surveillanceOfficerField = addField(CaseDataDto.SURVEILLANCE_OFFICER, ComboBox.class);
		surveillanceOfficerField.setNullSelectionAllowed(true);
		ComboBox region = addInfrastructureField(CaseDataDto.REGION);
		ComboBox district = addInfrastructureField(CaseDataDto.DISTRICT);
		ComboBox community = addInfrastructureField(CaseDataDto.COMMUNITY);
		community.setNullSelectionAllowed(true);
		community.addStyleName(SOFT_REQUIRED);
		facilityOrHome = new OptionGroup(I18nProperties.getCaption(Captions.casePlaceOfStay), TypeOfPlace.getTypesOfPlaceForCases());
		facilityOrHome.setId("facilityOrHome");
		facilityOrHome.setWidth(100, Unit.PERCENTAGE);
		CssStyles.style(facilityOrHome, ValoTheme.OPTIONGROUP_HORIZONTAL);
		getContent().addComponent(facilityOrHome, FACILITY_OR_HOME_LOC);
		facilityTypeGroup = new ComboBox();
		facilityTypeGroup.setId("typeGroup");
		facilityTypeGroup.setCaption(I18nProperties.getCaption(Captions.Facility_typeGroup));
		facilityTypeGroup.setWidth(100, Unit.PERCENTAGE);
		facilityTypeGroup.addItems(FacilityTypeGroup.getAccomodationGroups());
		facilityTypeGroup.setVisible(false);
		getContent().addComponent(facilityTypeGroup, TYPE_GROUP_LOC);
		facilityType = addField(CaseDataDto.FACILITY_TYPE);
//		type.setId("type");
//		type.setCaption(I18nProperties.getPrefixCaption(FacilityDto.I18N_PREFIX, FacilityDto.TYPE));
//		type.setWidth(100, Unit.PERCENTAGE);
		facilityType.setVisible(false);
//		getContent().addComponent(type, TYPE_LOC);
		ComboBox facility = addInfrastructureField(CaseDataDto.HEALTH_FACILITY);
		facility.setImmediate(true);
		facility.setVisible(false);
		TextField facilityDetails = addField(CaseDataDto.HEALTH_FACILITY_DETAILS, TextField.class);
		facilityDetails.setVisible(false);

		region.addValueChangeListener(e -> {
			RegionReferenceDto regionDto = (RegionReferenceDto) e.getProperty().getValue();
			FieldHelper
				.updateItems(district, regionDto != null ? FacadeProvider.getDistrictFacade().getAllActiveByRegion(regionDto.getUuid()) : null);
		});
		district.addValueChangeListener(e -> {
			DistrictReferenceDto districtDto = (DistrictReferenceDto) e.getProperty().getValue();
			FieldHelper.updateItems(
				community,
				districtDto != null ? FacadeProvider.getCommunityFacade().getAllActiveByDistrict(districtDto.getUuid()) : null);
			updateFacility(
				(DistrictReferenceDto) district.getValue(),
				(CommunityReferenceDto) community.getValue(),
				(FacilityType) facilityType.getValue(),
				facility);
		});
		community.addValueChangeListener(e -> {
			updateFacility(
				(DistrictReferenceDto) district.getValue(),
				(CommunityReferenceDto) community.getValue(),
				(FacilityType) facilityType.getValue(),
				facility);
		});
		facilityOrHome.addValueChangeListener(e -> {
			if (TypeOfPlace.FACILITY.equals(facilityOrHome.getValue())) {
				facilityTypeGroup.setVisible(true);
				facilityType.setVisible(true);
				facility.setVisible(true);

				// default values
				if (facilityTypeGroup.getValue() == null && !facilityTypeGroup.isReadOnly()) {
					facilityTypeGroup.setValue(FacilityTypeGroup.MEDICAL_FACILITY);
				}
				if (facilityType.getValue() == null
					&& FacilityTypeGroup.MEDICAL_FACILITY.equals(facilityTypeGroup.getValue())
					&& !facilityType.isReadOnly()) {
					facilityType.setValue(FacilityType.HOSPITAL);
				}

				if (CaseOrigin.IN_COUNTRY.equals(getField(CaseDataDto.CASE_ORIGIN).getValue())) {
					facility.setRequired(true);
				}
			} else {

				facilityTypeGroup.clear();
				facilityTypeGroup.setVisible(false);
				facilityType.setVisible(false);
				facility.setVisible(false);
				facility.setRequired(false);
			}
			updateFacilityDetails(facility, facilityDetails);
		});
		facilityTypeGroup.addValueChangeListener(e -> {
			FieldHelper.updateEnumData(facilityType, FacilityType.getAccommodationTypes((FacilityTypeGroup) facilityTypeGroup.getValue()));
		});
		facilityType.addValueChangeListener(e -> {
			updateFacility(
				(DistrictReferenceDto) district.getValue(),
				(CommunityReferenceDto) community.getValue(),
				(FacilityType) facilityType.getValue(),
				facility);
		});
		facility.addValueChangeListener(e -> {
			updateFacilityDetails(facility, facilityDetails);
		});
		region.addItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());

		if (!FacadeProvider.getFeatureConfigurationFacade().isFeatureDisabled(FeatureType.NATIONAL_CASE_SHARING)) {
			addField(CaseDataDto.SHARED_TO_COUNTRY, CheckBox.class);
			setReadOnly(!UserProvider.getCurrent().hasUserRight(UserRight.CASE_SHARE), CaseDataDto.SHARED_TO_COUNTRY);
		}

		addInfrastructureField(CaseDataDto.POINT_OF_ENTRY);
		addField(CaseDataDto.POINT_OF_ENTRY_DETAILS, TextField.class);

		TextField tfReportLat = addField(CaseDataDto.REPORT_LAT, TextField.class);
		tfReportLat.setConverter(new StringToAngularLocationConverter());
		TextField tfReportLon = addField(CaseDataDto.REPORT_LON, TextField.class);
		tfReportLon.setConverter(new StringToAngularLocationConverter());
		TextField tfReportAccuracy = addField(CaseDataDto.REPORT_LAT_LON_ACCURACY, TextField.class);

		Label generalCommentLabel = new Label(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.ADDITIONAL_DETAILS));
		generalCommentLabel.addStyleName(H3);
		getContent().addComponent(generalCommentLabel, GENERAL_COMMENT_LOC);

		TextArea additionalDetails = addField(CaseDataDto.ADDITIONAL_DETAILS, TextArea.class);
		additionalDetails.setRows(3);
		CssStyles.style(additionalDetails, CssStyles.CAPTION_HIDDEN);

		addField(CaseDataDto.PREGNANT, OptionGroup.class);
		addField(CaseDataDto.POSTPARTUM, OptionGroup.class);
		addField(CaseDataDto.TRIMESTER, OptionGroup.class);
		FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.TRIMESTER, CaseDataDto.PREGNANT, Arrays.asList(YesNoUnknown.YES), true);

		addFields(
			CaseDataDto.VACCINATION,
			CaseDataDto.VACCINATION_DOSES,
			CaseDataDto.VACCINATION_INFO_SOURCE,
			CaseDataDto.VACCINE,
			CaseDataDto.SMALLPOX_VACCINATION_SCAR,
			CaseDataDto.SMALLPOX_VACCINATION_RECEIVED,
			CaseDataDto.VACCINATION_DATE);

		// Set initial visibilities & accesses

		initializeVisibilitiesAndAllowedVisibilities();
		initializeAccessAndAllowedAccesses();

		// Set requirements that don't need visibility changes and read only status

		setRequired(
			true,
			CaseDataDto.REPORT_DATE,
			CaseDataDto.CASE_CLASSIFICATION,
			CaseDataDto.INVESTIGATION_STATUS,
			CaseDataDto.OUTCOME,
			CaseDataDto.DISEASE,
			CaseDataDto.REGION,
			CaseDataDto.DISTRICT);
		setSoftRequired(true, CaseDataDto.INVESTIGATED_DATE, CaseDataDto.OUTCOME_DATE, CaseDataDto.PLAGUE_TYPE, CaseDataDto.SURVEILLANCE_OFFICER);
		if (isEditableAllowed(CaseDataDto.INVESTIGATED_DATE)) {
			FieldHelper.setReadOnlyWhen(
				getFieldGroup(),
				CaseDataDto.INVESTIGATED_DATE,
				CaseDataDto.INVESTIGATION_STATUS,
				Arrays.asList(InvestigationStatus.PENDING),
				false,
				true);
		}
		setReadOnly(
			true,
			CaseDataDto.UUID,
			CaseDataDto.REPORTING_USER,
			CaseDataDto.CLASSIFICATION_USER,
			CaseDataDto.CLASSIFICATION_DATE,
			CaseDataDto.POINT_OF_ENTRY,
			CaseDataDto.POINT_OF_ENTRY_DETAILS,
			CaseDataDto.CASE_ORIGIN);
		setReadOnly(!UserProvider.getCurrent().hasUserRight(UserRight.CASE_CHANGE_DISEASE), CaseDataDto.DISEASE);
		setReadOnly(
			!UserProvider.getCurrent().hasUserRight(UserRight.CASE_INVESTIGATE),
			CaseDataDto.INVESTIGATION_STATUS,
			CaseDataDto.INVESTIGATED_DATE);
		setReadOnly(
			!UserProvider.getCurrent().hasUserRight(UserRight.CASE_CLASSIFY),
			CaseDataDto.CASE_CLASSIFICATION,
			CaseDataDto.OUTCOME,
			CaseDataDto.OUTCOME_DATE);
		setReadOnly(
			!UserProvider.getCurrent().hasUserRight(UserRight.CASE_TRANSFER) || !isEditableAllowed(CaseDataDto.COMMUNITY),
			CaseDataDto.REGION,
			CaseDataDto.DISTRICT);
		setReadOnly(
			!UserProvider.getCurrent().hasUserRight(UserRight.CASE_TRANSFER),
			CaseDataDto.COMMUNITY,
			FACILITY_OR_HOME_LOC,
			TYPE_GROUP_LOC,
			CaseDataDto.FACILITY_TYPE,
			CaseDataDto.HEALTH_FACILITY,
			CaseDataDto.HEALTH_FACILITY_DETAILS);

		// Set conditional visibilities - ALWAYS call isVisibleAllowed before
		// dynamically setting the visibility

		if (isVisibleAllowed(CaseDataDto.PREGNANT)) {
			setVisible(person.getSex() == Sex.FEMALE, CaseDataDto.PREGNANT, CaseDataDto.POSTPARTUM);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINATION_DOSES)) {
			FieldHelper
				.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINATION_DOSES, CaseDataDto.VACCINATION, Arrays.asList(Vaccination.VACCINATED), true);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINATION_INFO_SOURCE)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.VACCINATION_INFO_SOURCE,
				CaseDataDto.VACCINATION,
				Arrays.asList(Vaccination.VACCINATED),
				true);
		}
		if (isVisibleAllowed(CaseDataDto.DISEASE_DETAILS)) {
			FieldHelper
				.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.DISEASE_DETAILS), CaseDataDto.DISEASE, Arrays.asList(Disease.OTHER), true);
			FieldHelper
				.setRequiredWhen(getFieldGroup(), CaseDataDto.DISEASE, Arrays.asList(CaseDataDto.DISEASE_DETAILS), Arrays.asList(Disease.OTHER));
		}
		if (isVisibleAllowed(CaseDataDto.PLAGUE_TYPE)) {
			FieldHelper
				.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.PLAGUE_TYPE), CaseDataDto.DISEASE, Arrays.asList(Disease.PLAGUE), true);
		}
		if (isVisibleAllowed(CaseDataDto.DENGUE_FEVER_TYPE)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				Arrays.asList(CaseDataDto.DENGUE_FEVER_TYPE),
				CaseDataDto.DISEASE,
				Arrays.asList(Disease.DENGUE),
				true);
		}
		if (isVisibleAllowed(CaseDataDto.RABIES_TYPE)) {
			FieldHelper
				.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.RABIES_TYPE), CaseDataDto.DISEASE, Arrays.asList(Disease.RABIES), true);
		}
		if (isVisibleAllowed(CaseDataDto.SMALLPOX_VACCINATION_SCAR)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.SMALLPOX_VACCINATION_SCAR,
				CaseDataDto.SMALLPOX_VACCINATION_RECEIVED,
				Arrays.asList(YesNoUnknown.YES),
				true);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINATION_DATE)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.VACCINATION_DATE,
				CaseDataDto.SMALLPOX_VACCINATION_RECEIVED,
				Arrays.asList(YesNoUnknown.YES),
				true);
			FieldHelper
				.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINATION_DATE, CaseDataDto.VACCINATION, Arrays.asList(Vaccination.VACCINATED), true);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINE)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINE, CaseDataDto.VACCINATION, Arrays.asList(Vaccination.VACCINATED), true);
		}
		if (isVisibleAllowed(CaseDataDto.OUTCOME_DATE)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.OUTCOME_DATE,
				CaseDataDto.OUTCOME,
				Arrays.asList(CaseOutcome.DECEASED, CaseOutcome.RECOVERED),
				true);
		}
		if (isVisibleAllowed(CaseDataDto.SEQUELAE)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.SEQUELAE,
				CaseDataDto.OUTCOME,
				Arrays.asList(CaseOutcome.RECOVERED, CaseOutcome.UNKNOWN),
				true);
		}
		if (isVisibleAllowed(CaseDataDto.SEQUELAE_DETAILS)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.SEQUELAE_DETAILS, CaseDataDto.SEQUELAE, Arrays.asList(YesNoUnknown.YES), true);
		}
		if (isVisibleAllowed(CaseDataDto.NOTIFYING_CLINIC_DETAILS)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.NOTIFYING_CLINIC_DETAILS,
				CaseDataDto.NOTIFYING_CLINIC,
				Arrays.asList(HospitalWardType.OTHER),
				true);
		}

		/// CLINICIAN FIELDS
		if (UserProvider.getCurrent().hasUserRight(UserRight.CASE_MANAGEMENT_ACCESS)) {
			if (isVisibleAllowed(CaseDataDto.CLINICIAN_NAME)) {
				FieldHelper.setVisibleWhen(
					getFieldGroup(),
					CaseDataDto.CLINICIAN_NAME,
					CaseDataDto.FACILITY_TYPE,
					Arrays.asList(FacilityType.HOSPITAL, FacilityType.OTHER_MEDICAL_FACILITY),
					true);
			}
			if (isVisibleAllowed(CaseDataDto.CLINICIAN_PHONE)) {
				FieldHelper.setVisibleWhen(
					getFieldGroup(),
					CaseDataDto.CLINICIAN_PHONE,
					CaseDataDto.FACILITY_TYPE,
					Arrays.asList(FacilityType.HOSPITAL, FacilityType.OTHER_MEDICAL_FACILITY),
					true);
			}
			if (isVisibleAllowed(CaseDataDto.CLINICIAN_EMAIL)) {
				FieldHelper.setVisibleWhen(
					getFieldGroup(),
					CaseDataDto.CLINICIAN_EMAIL,
					CaseDataDto.FACILITY_TYPE,
					Arrays.asList(FacilityType.HOSPITAL, FacilityType.OTHER_MEDICAL_FACILITY),
					true);
			}
		} else {
			setVisible(false, CaseDataDto.CLINICIAN_NAME, CaseDataDto.CLINICIAN_PHONE, CaseDataDto.CLINICIAN_EMAIL);
		}

		// Other initializations

		if (disease == Disease.MONKEYPOX) {
			Image smallpoxVaccinationScarImg = new Image(null, new ThemeResource("img/smallpox-vaccination-scar.jpg"));
			style(smallpoxVaccinationScarImg, VSPACE_3);
			getContent().addComponent(smallpoxVaccinationScarImg, SMALLPOX_VACCINATION_SCAR_IMG);

			// Set up initial image visibility
			getContent().getComponent(SMALLPOX_VACCINATION_SCAR_IMG)
				.setVisible(getFieldGroup().getField(CaseDataDto.SMALLPOX_VACCINATION_RECEIVED).getValue() == YesNoUnknown.YES);

			// Set up image visibility listener
			getFieldGroup().getField(CaseDataDto.SMALLPOX_VACCINATION_RECEIVED).addValueChangeListener(e -> {
				getContent().getComponent(SMALLPOX_VACCINATION_SCAR_IMG).setVisible(e.getProperty().getValue() == YesNoUnknown.YES);
			});
		}

		List<String> medicalInformationFields =
			Arrays.asList(CaseDataDto.PREGNANT, CaseDataDto.VACCINATION, CaseDataDto.SMALLPOX_VACCINATION_RECEIVED);

		for (String medicalInformationField : medicalInformationFields) {
			if (getFieldGroup().getField(medicalInformationField).isVisible()) {
				Label medicalInformationCaptionLabel = new Label(I18nProperties.getString(Strings.headingMedicalInformation));
				medicalInformationCaptionLabel.addStyleName(H3);
				getContent().addComponent(medicalInformationCaptionLabel, MEDICAL_INFORMATION_LOC);
				break;
			}
		}

		Label paperFormDatesLabel = new Label(I18nProperties.getString(Strings.headingPaperFormDates));
		paperFormDatesLabel.addStyleName(H3);
		getContent().addComponent(paperFormDatesLabel, PAPER_FORM_DATES_LOC);

		// Automatic case classification rules button - invisible for other diseases
		DiseaseClassificationCriteriaDto diseaseClassificationCriteria = FacadeProvider.getCaseClassificationFacade().getByDisease(disease);
		if (disease != Disease.OTHER && diseaseClassificationCriteria != null) {
			Button classificationRulesButton = ButtonHelper.createIconButton(Captions.info, VaadinIcons.INFO_CIRCLE, e -> {
				ControllerProvider.getCaseController().openClassificationRulesPopup(diseaseClassificationCriteria);
			}, ValoTheme.BUTTON_PRIMARY, FORCE_CAPTION);

			getContent().addComponent(classificationRulesButton, CLASSIFICATION_RULES_LOC);
		}

		addValueChangeListener(e -> {
			diseaseField.addValueChangeListener(new DiseaseChangeListener(diseaseField, getValue().getDisease()));
			surveillanceOfficerField
				.addItems(FacadeProvider.getUserFacade().getUserRefsByDistrict(getValue().getDistrict(), false, UserRole.SURVEILLANCE_OFFICER));

			// Replace classification user if case has been automatically classified
			if (getValue().getClassificationDate() != null && getValue().getClassificationUser() == null) {
				getField(CaseDataDto.CLASSIFICATION_USER).setVisible(false);
				Label classifiedBySystemLabel = new Label(I18nProperties.getCaption(Captions.system));
				classifiedBySystemLabel.setCaption(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.CLASSIFIED_BY));
				getContent().addComponent(classifiedBySystemLabel, CLASSIFIED_BY_SYSTEM_LOC);
			}

			setEpidNumberError(epidField, assignNewEpidNumberButton, epidNumberWarningLabel, getValue().getEpidNumber());

			epidField.addValueChangeListener(f -> {
				setEpidNumberError(epidField, assignNewEpidNumberButton, epidNumberWarningLabel, (String) f.getProperty().getValue());
			});

			// Set health facility details visibility and caption
			if (getValue().getHealthFacility() != null) {
				boolean otherHealthFacility = getValue().getHealthFacility().getUuid().equals(FacilityDto.OTHER_FACILITY_UUID);
				boolean noneHealthFacility = getValue().getHealthFacility().getUuid().equals(FacilityDto.NONE_FACILITY_UUID);

				FacilityType caseFacilityType = getValue().getFacilityType();
				if (noneHealthFacility || caseFacilityType == null) {
					facilityOrHome.setValue(TypeOfPlace.HOME);
				} else {
					facilityOrHome.setValue(TypeOfPlace.FACILITY);
					facilityTypeGroup.setValue(caseFacilityType.getFacilityTypeGroup());
					if (!facilityType.isReadOnly()) {
						facilityType.setValue(caseFacilityType);
					}
				}
			}

			// Set health facility/point of entry visibility based on case origin
			if (getValue().getCaseOrigin() == CaseOrigin.POINT_OF_ENTRY) {
				setVisible(true, CaseDataDto.POINT_OF_ENTRY);
				setVisible(getValue().getPointOfEntry().isOtherPointOfEntry(), CaseDataDto.POINT_OF_ENTRY_DETAILS);

				if (getValue().getHealthFacility() == null) {
					setVisible(
						false,
						CaseDataDto.COMMUNITY,
						FACILITY_OR_HOME_LOC,
						TYPE_GROUP_LOC,
						CaseDataDto.FACILITY_TYPE,
						CaseDataDto.HEALTH_FACILITY,
						CaseDataDto.HEALTH_FACILITY_DETAILS);
					setReadOnly(true, CaseDataDto.REGION, CaseDataDto.DISTRICT, CaseDataDto.COMMUNITY);
				}
			} else {
				setRequired(true, CaseDataDto.HEALTH_FACILITY);
				facilityOrHome.setRequired(true);
				facilityTypeGroup.setRequired(true);
				facilityType.setRequired(true);
				setVisible(false, CaseDataDto.POINT_OF_ENTRY, CaseDataDto.POINT_OF_ENTRY_DETAILS);
			}

			// take over the value that has been set based on access rights
			facilityTypeGroup.setReadOnly(facilityType.isReadOnly());
			facilityOrHome.setReadOnly(facilityType.isReadOnly());

			// Hide case origin from port health users
			if (UserRole.isPortHealthUser(UserProvider.getCurrent().getUserRoles())) {
				setVisible(false, CaseDataDto.CASE_ORIGIN);
			}

			if (isGermanServer()) {
				setVisible(false, CaseDataDto.EPID_NUMBER);
			} else {
				setVisible(false, CaseDataDto.EXTERNAL_ID);
			}
		});
	}

	@Override
	public void setValue(CaseDataDto newFieldValue) throws ReadOnlyException, ConversionException {
		super.setValue(newFieldValue);

		// HACK: Binding to the fields will call field listeners that may clear/modify the values of other fields.
		// this hopefully resets everything to it's correct value
		discard();
	}

	private void updateFacility(DistrictReferenceDto district, CommunityReferenceDto community, FacilityType facilityType, ComboBox facility) {
		if (facilityType != null) {
			if (community != null) {
				FieldHelper.updateItems(
					facility,
					FacadeProvider.getFacilityFacade().getActiveFacilitiesByCommunityAndType(community, facilityType, true, false));
			} else if (district != null) {
				FieldHelper.updateItems(
					facility,
					FacadeProvider.getFacilityFacade().getActiveFacilitiesByDistrictAndType(district, facilityType, true, false));
			} else {
				FieldHelper.removeItems(facility);

			}
		} else {
			if (TypeOfPlace.HOME.equals(facilityOrHome.getValue())) {
				FacilityReferenceDto noFacilityRef = FacadeProvider.getFacilityFacade().getByUuid(FacilityDto.NONE_FACILITY_UUID).toReference();
				facility.addItem(noFacilityRef);
				facility.setValue(noFacilityRef);
			} else {
				FieldHelper.removeItems(facility);
			}
		}
	}

	private void updateQuarantineFields() {
		QuarantineType quarantineType = (QuarantineType) quarantine.getValue();
		boolean visible;
		if (QuarantineType.HOME.equals(quarantineType) || QuarantineType.INSTITUTIONELL.equals(quarantineType)) {
			visible = true;
		} else {
			visible = false;
			quarantineFrom.clear();
			quarantineTo.clear();
			if (isGermanServer()) {
				quarantineOrderedVerbally.clear();
				quarantineOrderedOfficialDocument.clear();
			}
		}

		if (isGermanServer()) {
			setVisible(visible, CaseDataDto.QUARANTINE_ORDERED_VERBALLY, CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT);
		}
		setVisible(visible, CaseDataDto.QUARANTINE_FROM, CaseDataDto.QUARANTINE_TO, CaseDataDto.QUARANTINE_HELP_NEEDED);
	}

	private void updateFacilityDetails(ComboBox cbFacility, TextField tfFacilityDetails) {
		if (cbFacility.getValue() != null) {
			boolean otherHealthFacility = ((FacilityReferenceDto) cbFacility.getValue()).getUuid().equals(FacilityDto.OTHER_FACILITY_UUID);
			boolean noneHealthFacility = ((FacilityReferenceDto) cbFacility.getValue()).getUuid().equals(FacilityDto.NONE_FACILITY_UUID);
			boolean visibleAndRequired = otherHealthFacility || noneHealthFacility;

			tfFacilityDetails.setVisible(visibleAndRequired);
			tfFacilityDetails.setRequired(visibleAndRequired);

			if (otherHealthFacility) {
				tfFacilityDetails.setCaption(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.HEALTH_FACILITY_DETAILS));
			}
			if (noneHealthFacility) {
				tfFacilityDetails.setCaption(I18nProperties.getCaption(Captions.CaseData_noneHealthFacilityDetails));
			}
			if (!visibleAndRequired && !tfFacilityDetails.isReadOnly()) {
				tfFacilityDetails.clear();
			}
		} else {
			tfFacilityDetails.setVisible(false);
			tfFacilityDetails.setRequired(false);
			if (!tfFacilityDetails.isReadOnly()) {
				tfFacilityDetails.clear();
			}
		}
	}

	@Override
	protected String createHtmlLayout() {
		return HTML_LAYOUT;
	}

	private void setEpidNumberError(TextField epidField, Button assignNewEpidNumberButton, Label epidNumberWarningLabel, String fieldValue) {
		if (!isGermanServer() && FacadeProvider.getCaseFacade().doesEpidNumberExist(fieldValue, getValue().getUuid(), getValue().getDisease())) {
			epidField.setComponentError(new UserError(I18nProperties.getValidationError(Validations.duplicateEpidNumber)));
			assignNewEpidNumberButton.setVisible(true);
			getContent().addComponent(epidNumberWarningLabel, EPID_NUMBER_WARNING_LOC);
		} else {
			epidField.setComponentError(null);
			getContent().removeComponent(epidNumberWarningLabel);
			assignNewEpidNumberButton
				.setVisible(!isGermanServer() && !CaseLogic.isEpidNumberPrefix(fieldValue) && !CaseLogic.isCompleteEpidNumber(fieldValue));
		}
	}

	private static class DiseaseChangeListener implements ValueChangeListener {

		private static final long serialVersionUID = -5339850320902885768L;

		private final AbstractSelect diseaseField;
		private final Disease currentDisease;

		DiseaseChangeListener(AbstractSelect diseaseField, Disease currentDisease) {
			this.diseaseField = diseaseField;
			this.currentDisease = currentDisease;
		}

		@Override
		public void valueChange(Property.ValueChangeEvent e) {

			if (diseaseField.getValue() != currentDisease) {
				ConfirmationComponent confirmDiseaseChangeComponent = new ConfirmationComponent(false) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void onConfirm() {
						diseaseField.removeValueChangeListener(DiseaseChangeListener.this);
					}

					@Override
					protected void onCancel() {
						diseaseField.setValue(currentDisease);
					}
				};
				confirmDiseaseChangeComponent.getConfirmButton().setCaption(I18nProperties.getString(Strings.confirmationChangeCaseDisease));
				confirmDiseaseChangeComponent.getCancelButton().setCaption(I18nProperties.getCaption(Captions.actionCancel));
				confirmDiseaseChangeComponent.setMargin(true);

				Window popupWindow = VaadinUiUtil.showPopupWindow(confirmDiseaseChangeComponent);
				CloseListener closeListener = ce -> diseaseField.setValue(currentDisease);
				popupWindow.addCloseListener(closeListener);
				confirmDiseaseChangeComponent.addDoneListener(new DoneListener() {

					public void onDone() {
						popupWindow.removeCloseListener(closeListener);
						popupWindow.close();
					}
				});
				popupWindow.setCaption(I18nProperties.getString(Strings.headingChangeCaseDisease));
			}
		}
	}
}
