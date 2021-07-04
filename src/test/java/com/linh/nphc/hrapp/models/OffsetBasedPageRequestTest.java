package com.linh.nphc.hrapp.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
public class OffsetBasedPageRequestTest {

    @Test
    public void shouldEqual(){
        OffsetBasedPageRequest pageRequest1 = new OffsetBasedPageRequest(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        OffsetBasedPageRequest pageRequest2 = new OffsetBasedPageRequest(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        assertTrue(pageRequest1.equals(pageRequest2));
        assertTrue(pageRequest1.equals(pageRequest1));
    }

    @Test
    public void shouldNotEqual(){
        OffsetBasedPageRequest pageRequest1 = new OffsetBasedPageRequest(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        OffsetBasedPageRequest pageRequest2 = new OffsetBasedPageRequest(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        assertFalse(pageRequest1.equals(pageRequest2));
    }

    @Test
    public void shouldProduceHashCode(){
        OffsetBasedPageRequest pageRequest1 = new OffsetBasedPageRequest(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        assertNotNull(pageRequest1.hashCode());
    }

    @Test
    public void shouldProduceToString(){
        OffsetBasedPageRequest pageRequest1 = new OffsetBasedPageRequest(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        assertNotNull(pageRequest1.toString());
    }
}