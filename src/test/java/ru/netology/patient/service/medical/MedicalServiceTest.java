package ru.netology.patient.service.medical;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class MedicalServiceTest {

    @Mock
    private PatientInfoRepository patientInfoRepository;
    @Spy
    private SendAlertService alertService;
    @Captor
    private ArgumentCaptor<String> captor;

    private MedicalService medicalService;

    private static Stream<Arguments> provideBadBloodPressure() {
        return Stream.of(
                //id, bloodPressure
                Arguments.of("1", new BloodPressure(120, 60)),
                Arguments.of("2", new BloodPressure(120, 78))
        );
    }

    private static Stream<Arguments> provideBadTemperature() {
        return Stream.of(
                //id, temperature
                Arguments.of("1", new BigDecimal("34.9")),
                Arguments.of("2", new BigDecimal("35.0"))
        );
    }

    private static Stream<Arguments> provideGoodData() {
        return Stream.of(
                //id, bloodPressure, temperature
                Arguments.of("1", new BloodPressure(120, 80), new BigDecimal("36.9")),
                Arguments.of("2", new BloodPressure(125, 78), new BigDecimal("35.1"))
        );
    }

    @BeforeEach
    void setup() {
        Mockito.lenient().when(patientInfoRepository.getById("1")).thenReturn(
                new PatientInfo("1", "Иван", "Петров", LocalDate.of(1980, 11, 26),
                        new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80)))
        );
        Mockito.lenient().when(patientInfoRepository.getById("2")).thenReturn(
                new PatientInfo("2", "Семен", "Михайлов", LocalDate.of(1982, 1, 16),
                        new HealthInfo(new BigDecimal("36.6"), new BloodPressure(125, 78)))
        );

        medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
    }

    @ParameterizedTest
    @DisplayName("Проверка вывода сообщения во время проверки давления")
    @MethodSource("provideBadBloodPressure")
    void shouldSendMessageOnBloodPressureChecking(String id, BloodPressure bloodPressure) {
        medicalService.checkBloodPressure(id, bloodPressure);

        Mockito.verify(alertService).send(captor.capture());
        Assertions.assertEquals(String.format("Warning, patient with id: %s, need help", id), captor.getValue());
    }

    @ParameterizedTest
    @DisplayName("Проверка вывода сообщения во время проверки температуры")
    @MethodSource("provideBadTemperature")
    void shouldSendMessageOnTemperatureChecking(String id, BigDecimal temperature) {
        medicalService.checkTemperature(id, temperature);

        Mockito.verify(alertService).send(captor.capture());
        Assertions.assertEquals(String.format("Warning, patient with id: %s, need help", id), captor.getValue());
    }

    @ParameterizedTest
    @DisplayName("Проверка не вывода сообщения при корректных данных")
    @MethodSource("provideGoodData")
    void shouldNotSendMessageOnGoodData(String id, BloodPressure bloodPressure, BigDecimal temperature) {
        medicalService.checkBloodPressure(id, bloodPressure);
        medicalService.checkTemperature(id, temperature);

        Mockito.verify(alertService, Mockito.never()).send(Mockito.any());
    }
}
