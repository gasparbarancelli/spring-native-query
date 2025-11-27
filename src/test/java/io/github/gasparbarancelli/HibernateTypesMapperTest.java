package io.github.gasparbarancelli;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class HibernateTypesMapperTest {

    static class TestDto {
        public Integer id;
        public String name;
        public Boolean active;
    }

    @Test
    void testMap_AddsScalarsCorrectly() {
        NativeQuery<?> query = mock(NativeQuery.class);

        HibernateTypesMapper.map(query, TestDto.class);

        verify(query).addScalar("id", StandardBasicTypes.INTEGER);
        verify(query).addScalar("name", StandardBasicTypes.STRING);
        verify(query).addScalar("active", StandardBasicTypes.BOOLEAN);
        verifyNoMoreInteractions(query);
    }
}
