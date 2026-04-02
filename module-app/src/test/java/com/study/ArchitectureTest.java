package com.study;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  private static JavaClasses classes;

  @BeforeAll
  static void setUp() {
    classes =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.study");
  }

  /** 모듈에 아직 클래스가 없어도 테스트가 깨지지 않도록 허용. 실제 클래스가 추가되면 의존성 위반을 정상 검증한다. */
  private static final boolean ALLOW_EMPTY = true;

  @Nested
  @DisplayName("모듈 간 의존성 규칙")
  class ModuleBoundaryTest {

    @Test
    @DisplayName("blog 모듈은 다른 팀 모듈에 의존할 수 없다")
    void blogShouldNotDependOnOtherTeamModules() {
      noClasses()
          .that()
          .resideInAPackage("..blog..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..qna..", "..profile..", "..sessionboard..")
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }

    @Test
    @DisplayName("qna 모듈은 다른 팀 모듈에 의존할 수 없다")
    void qnaShouldNotDependOnOtherTeamModules() {
      noClasses()
          .that()
          .resideInAPackage("..qna..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..blog..", "..profile..", "..sessionboard..")
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }

    @Test
    @DisplayName("profile 모듈은 다른 팀 모듈에 의존할 수 없다")
    void profileShouldNotDependOnOtherTeamModules() {
      noClasses()
          .that()
          .resideInAPackage("..profile..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..blog..", "..qna..", "..sessionboard..")
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }

    @Test
    @DisplayName("session-board 모듈은 다른 팀 모듈에 의존할 수 없다")
    void sessionBoardShouldNotDependOnOtherTeamModules() {
      noClasses()
          .that()
          .resideInAPackage("..sessionboard..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..blog..", "..qna..", "..profile..")
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }
  }

  @Nested
  @DisplayName("contract 모듈 규칙")
  class ContractModuleTest {

    @Test
    @DisplayName("contract 모듈은 팀 모듈에 의존할 수 없다")
    void contractShouldNotDependOnTeamModules() {
      noClasses()
          .that()
          .resideInAPackage("..contract..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..blog..", "..qna..", "..profile..", "..sessionboard..")
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }
  }

  @Nested
  @DisplayName("common 모듈 규칙")
  class CommonModuleTest {

    @Test
    @DisplayName("common 모듈은 팀 모듈과 contract에 의존할 수 없다")
    void commonShouldNotDependOnTeamOrContractModules() {
      noClasses()
          .that()
          .resideInAPackage("..common..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..blog..", "..qna..", "..profile..", "..sessionboard..", "..contract..")
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }
  }
}
