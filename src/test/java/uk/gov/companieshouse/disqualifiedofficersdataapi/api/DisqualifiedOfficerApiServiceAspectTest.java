package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.FeatureFlags;

@ExtendWith(MockitoExtension.class)
class DisqualifiedOfficerApiServiceAspectTest {

    @Mock
    private FeatureFlags featureFlags;

    @InjectMocks
    private DisqualifiedOfficerApiServiceAspect aspect;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private Object object;

    @Test
    void testAspectProceedsWhenFlagEnabled() throws Throwable {
        // given
        when(featureFlags.isStreamHookEnabled()).thenReturn(true);
        when(proceedingJoinPoint.proceed()).thenReturn(object);
        // when
        Object actual = aspect.invokeChsKafkaApi(proceedingJoinPoint);

        // then
        assertSame(object, actual);
        verify(proceedingJoinPoint).proceed();
    }

    @Test
    void testAspectDoesNotProceedWhenFlagDisabled() throws Throwable {
        // when
        Object actual = aspect.invokeChsKafkaApi(proceedingJoinPoint);

        // then
        assertNull(actual);
        verifyNoInteractions(proceedingJoinPoint);
    }
}
