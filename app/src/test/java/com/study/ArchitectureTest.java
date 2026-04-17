package com.study;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Set;
import java.util.stream.Collectors;
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
          .and()
          .resideOutsideOfPackage("..contract..")
          .should()
          .dependOnClassesThat(
              resideInAnyPackage("..qna..", "..profile..", "..sessionboard..")
                  .and(resideOutsideOfPackage("..contract..")))
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }

    @Test
    @DisplayName("qna 모듈은 다른 팀 모듈에 의존할 수 없다")
    void qnaShouldNotDependOnOtherTeamModules() {
      noClasses()
          .that()
          .resideInAPackage("..qna..")
          .and()
          .resideOutsideOfPackage("..contract..")
          .should()
          .dependOnClassesThat(
              resideInAnyPackage("..blog..", "..profile..", "..sessionboard..")
                  .and(resideOutsideOfPackage("..contract..")))
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }

    @Test
    @DisplayName("profile 모듈은 다른 팀 모듈에 의존할 수 없다")
    void profileShouldNotDependOnOtherTeamModules() {
      noClasses()
          .that()
          .resideInAPackage("..profile..")
          .and()
          .resideOutsideOfPackage("..contract..")
          .should()
          .dependOnClassesThat(
              resideInAnyPackage("..blog..", "..qna..", "..sessionboard..")
                  .and(resideOutsideOfPackage("..contract..")))
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }

    @Test
    @DisplayName("session-board 모듈은 다른 팀 모듈에 의존할 수 없다")
    void sessionBoardShouldNotDependOnOtherTeamModules() {
      noClasses()
          .that()
          .resideInAPackage("..sessionboard..")
          .and()
          .resideOutsideOfPackage("..contract..")
          .should()
          .dependOnClassesThat(
              resideInAnyPackage("..blog..", "..qna..", "..profile..")
                  .and(resideOutsideOfPackage("..contract..")))
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
  @DisplayName("Port 규칙")
  class PortTest {

    @Test
    @DisplayName("contract의 인터페이스는 Port로 끝나야 한다")
    void contractInterfacesShouldEndWithPort() {
      classes()
          .that()
          .resideInAPackage("..contract..")
          .and()
          .areInterfaces()
          .should()
          .haveSimpleNameEndingWith("Port")
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }

    @Test
    @DisplayName("Port 구현체는 다른 모듈의 Port에 의존할 수 없다")
    void portImplShouldNotDependOnOtherPorts() {
      classes()
          .that()
          .implement(simpleNameEndingWith("Port"))
          .should(notDependOnOtherPorts())
          .allowEmptyShould(ALLOW_EMPTY)
          .check(classes);
    }

    private ArchCondition<JavaClass> notDependOnOtherPorts() {
      return new ArchCondition<>("not depend on other Port interfaces") {
        @Override
        public void check(JavaClass item, ConditionEvents events) {
          Set<String> ownPorts =
              item.getInterfaces().stream()
                  .map(JavaType::toErasure)
                  .filter(i -> i.getSimpleName().endsWith("Port"))
                  .map(JavaClass::getFullName)
                  .collect(Collectors.toSet());

          item.getDirectDependenciesFromSelf().stream()
              .map(Dependency::getTargetClass)
              .filter(target -> target.getSimpleName().endsWith("Port"))
              .filter(target -> target.getPackageName().contains("contract"))
              .filter(target -> !ownPorts.contains(target.getFullName()))
              .distinct()
              .forEach(
                  target ->
                      events.add(
                          SimpleConditionEvent.violated(
                              item,
                              String.format(
                                  "%s이(가) 다른 모듈의 %s에 의존합니다",
                                  item.getSimpleName(), target.getSimpleName()))));
        }
      };
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
