package cc.wutao.aspect;

import cc.wutao.annotation.AutoFill;
import cc.wutao.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AutoFillAspectTest {

    private final AutoFillAspect aspect = new AutoFillAspect();

    @Test
    void insertSetsCreateAndUpdateTime() throws Exception {
        AuditedEntity entity = new AuditedEntity();

        aspect.autoFill(joinPoint("insert", entity));

        assertNotNull(entity.createTime);
        assertEquals(entity.createTime, entity.updateTime);
    }

    @Test
    void updateOnlySetsUpdateTime() throws Exception {
        AuditedEntity entity = new AuditedEntity();

        aspect.autoFill(joinPoint("update", entity));

        assertNull(entity.createTime);
        assertNotNull(entity.updateTime);
    }

    @Test
    void missingAuditSetterFailsFast() throws Exception {
        JoinPoint joinPoint = joinPoint("insert", new Object());

        assertThrows(IllegalStateException.class, () -> aspect.autoFill(joinPoint));
    }

    private JoinPoint joinPoint(String methodName, Object entity) throws Exception {
        Method method = TestMapper.class.getDeclaredMethod(methodName, Object.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);

        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{entity});
        return joinPoint;
    }

    private interface TestMapper {

        @AutoFill(OperationType.INSERT)
        void insert(Object entity);

        @AutoFill(OperationType.UPDATE)
        void update(Object entity);
    }

    private static class AuditedEntity {

        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }
    }
}
