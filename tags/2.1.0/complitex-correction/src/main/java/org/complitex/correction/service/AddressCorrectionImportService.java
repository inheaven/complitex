package org.complitex.correction.service;

import au.com.bytecode.opencsv.CSVReader;
import org.complitex.address.entity.AddressImportFile;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.city.CityStrategy;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.address.strategy.street.StreetStrategy;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;
import org.complitex.dictionary.service.AbstractImportService;
import org.complitex.dictionary.service.IImportListener;
import org.complitex.dictionary.service.exception.ImportFileNotFoundException;
import org.complitex.dictionary.service.exception.ImportFileReadException;
import org.complitex.dictionary.service.exception.ImportObjectLinkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.02.11 16:08
 */
@Stateless
public class AddressCorrectionImportService extends AbstractImportService {

    private final static Logger log = LoggerFactory.getLogger(AddressCorrectionImportService.class);
    @EJB
    private AddressCorrectionBean addressCorrectionBean;
    @EJB
    private CityStrategy cityStrategy;
    @EJB
    private DistrictStrategy districtStrategy;
    @EJB
    private StreetTypeStrategy streetTypeStrategy;
    @EJB
    private StreetStrategy streetStrategy;
    @EJB
    private BuildingStrategy buildingStrategy;

    public void process(long organizationId, long internalOrganizationId, IImportListener listener)
            throws ImportFileNotFoundException, ImportObjectLinkException, ImportFileReadException {
        importCityToCorrection(organizationId, internalOrganizationId, listener);
        importDistrictToCorrection(organizationId, internalOrganizationId, listener);
        importStreetTypeToCorrection(organizationId, internalOrganizationId, listener);
        importStreetToCorrection(organizationId, internalOrganizationId, listener);
        importBuildingToCorrection(organizationId, internalOrganizationId, listener);
    }

    /**
     * CITY_ID	REGION_ID	CITY_TYPE_ID	Название населенного пункта
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importCityToCorrection(Long orgId, Long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(AddressImportFile.CITY, getRecordCount(AddressImportFile.CITY));

        CSVReader reader = getCsvReader(AddressImportFile.CITY);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //CITY_ID
                Long objectId = cityStrategy.getObjectId(Long.parseLong(line[0].trim()));
                if (objectId == null) {
                    throw new ImportObjectLinkException(AddressImportFile.CITY.getFileName(), recordIndex, line[0]);
                }

                addressCorrectionBean.insertCityCorrection(line[3].trim(), objectId, orgId, intOrgId, null);

                listener.recordProcessed(AddressImportFile.CITY, recordIndex);
            }

            listener.completeImport(AddressImportFile.CITY, recordIndex);
        } catch (IOException e) {
            throw new ImportFileReadException(e, AddressImportFile.CITY.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, AddressImportFile.CITY.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * DISTRICT_ID	CITY_ID	Код района	Название района
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importDistrictToCorrection(Long orgId, Long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(AddressImportFile.DISTRICT, getRecordCount(AddressImportFile.DISTRICT));

        CSVReader reader = getCsvReader(AddressImportFile.DISTRICT);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //DISTRICT_ID
                Long districtId = districtStrategy.getObjectId(Long.parseLong(line[0].trim()));
                if (districtId == null) {
                    throw new ImportObjectLinkException(AddressImportFile.DISTRICT.getFileName(), recordIndex, line[0]);
                }

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (cityId == null) {
                    throw new ImportObjectLinkException(AddressImportFile.DISTRICT.getFileName(), recordIndex, line[1]);
                }

                //City Correction
                Long cityCorrectionId = addressCorrectionBean.getCityCorrectionId(cityId, orgId, intOrgId);
                if (cityCorrectionId == null) {
                    throw new ImportObjectLinkException(AddressImportFile.DISTRICT.getFileName(), recordIndex, line[1]);
                }

                addressCorrectionBean.insertDistrictCorrection(line[3].trim(), cityCorrectionId, districtId, orgId,
                        intOrgId, null);

                listener.recordProcessed(AddressImportFile.DISTRICT, recordIndex);
            }

            listener.completeImport(AddressImportFile.DISTRICT, recordIndex);
        } catch (IOException e) {
            throw new ImportFileReadException(e, AddressImportFile.DISTRICT.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, AddressImportFile.DISTRICT.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * STREET_TYPE_ID	Короткое наименование	Название типа улицы
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importStreetTypeToCorrection(Long orgId, Long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(AddressImportFile.STREET_TYPE, getRecordCount(AddressImportFile.STREET_TYPE));

        CSVReader reader = getCsvReader(AddressImportFile.STREET_TYPE);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[0].trim()));
                if (streetTypeId == null) {
                    throw new ImportObjectLinkException(AddressImportFile.STREET_TYPE.getFileName(), recordIndex, line[0]);
                }

                addressCorrectionBean.insertStreetTypeCorrection(line[2].trim(), streetTypeId, orgId, intOrgId, null);

                listener.recordProcessed(AddressImportFile.STREET_TYPE, recordIndex);
            }

            listener.completeImport(AddressImportFile.STREET_TYPE, recordIndex);
        } catch (IOException e) {
            throw new ImportFileReadException(e, AddressImportFile.STREET_TYPE.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, AddressImportFile.STREET_TYPE.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * STREET_ID	CITY_ID	STREET_TYPE_ID	Название улицы
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importStreetToCorrection(Long orgId, Long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(AddressImportFile.STREET, getRecordCount(AddressImportFile.STREET));

        CSVReader reader = getCsvReader(AddressImportFile.STREET);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                //STREET_ID
                Long streetId = streetStrategy.getObjectId(Long.parseLong(line[0].trim()));

                //CITY_ID
                Long cityId = cityStrategy.getObjectId(Long.parseLong(line[1].trim()));
                if (cityId == null) {
                    throw new ImportObjectLinkException(AddressImportFile.STREET.getFileName(), recordIndex, line[1]);
                }

                //City Correction
                Long cityCorrectionId = addressCorrectionBean.getCityCorrectionId(cityId, orgId, intOrgId);
                if (cityCorrectionId == null) {
                    throw new ImportObjectLinkException(AddressImportFile.STREET.getFileName(), recordIndex, line[1]);
                }

                //STREET_TYPE_ID
                Long streetTypeId = streetTypeStrategy.getObjectId(Long.parseLong(line[2].trim()));
                if (streetTypeId == null) {
                    throw new ImportObjectLinkException(AddressImportFile.STREET.getFileName(), recordIndex, line[2]);
                }

                //Street Type Correction
                Long streetTypeCorrectionId = addressCorrectionBean.getStreetTypeCorrectionId(streetTypeId, orgId, intOrgId);

                addressCorrectionBean.insertStreetCorrection(line[3].trim(), null, streetTypeCorrectionId,
                        cityCorrectionId, streetId, orgId, intOrgId, null);

                listener.recordProcessed(AddressImportFile.STREET, recordIndex);
            }

            listener.completeImport(AddressImportFile.STREET, recordIndex);
        } catch (IOException e) {
            throw new ImportFileReadException(e, AddressImportFile.STREET.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, AddressImportFile.STREET.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }

    /**
     * BUILDING_ID	DISTRICT_ID	STREET_ID	Номер дома	Корпус	Строение
     * @throws ImportFileNotFoundException
     * @throws ImportFileReadException
     */
    public void importBuildingToCorrection(Long orgId, Long intOrgId, IImportListener listener)
            throws ImportFileNotFoundException, ImportFileReadException, ImportObjectLinkException {
        listener.beginImport(AddressImportFile.BUILDING, getRecordCount(AddressImportFile.BUILDING));

        CSVReader reader = getCsvReader(AddressImportFile.BUILDING);

        int recordIndex = 0;

        try {
            String[] line;

            while ((line = reader.readNext()) != null) {
                recordIndex++;

                Long buildingId = buildingStrategy.getObjectId(Long.parseLong(line[0]));

                //STREET_ID
                Long streetId = streetStrategy.getObjectId(Long.parseLong(line[2].trim()));
                if (streetId == null) {
                    throw new ImportObjectLinkException(AddressImportFile.BUILDING.getFileName(), recordIndex, line[2]);
                }

                //Street Correction
                Long streetCorrectionId = addressCorrectionBean.getStreetCorrectionId(streetId, orgId, intOrgId);
                if (streetCorrectionId == null) {
                    throw new ImportObjectLinkException(AddressImportFile.BUILDING.getFileName(), recordIndex, line[2]);
                }

                addressCorrectionBean.insertBuildingCorrection(line[3].trim(), line[4].trim(), streetCorrectionId,
                        buildingId, orgId, intOrgId, null);

                listener.recordProcessed(AddressImportFile.BUILDING, recordIndex);
            }

            listener.completeImport(AddressImportFile.BUILDING, recordIndex);
        } catch (IOException e) {
            throw new ImportFileReadException(e, AddressImportFile.BUILDING.getFileName(), recordIndex);
        } catch (NumberFormatException e) {
            throw new ImportFileReadException(e, AddressImportFile.BUILDING.getFileName(), recordIndex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Ошибка закрытия потока", e);
            }
        }
    }
}