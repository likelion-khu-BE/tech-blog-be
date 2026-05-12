package com.study;

import com.study.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfig.class)
class StudyBeApplicationTests {

  @Test
  void contextLoads() {}
}
