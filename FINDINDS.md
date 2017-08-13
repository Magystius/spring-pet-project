# Findings

- Spring bietet volle [JSR-303](http://beanvalidation.org/1.0/spec/) Unterstützung
- [Hibernate Validator](http://hibernate.org/validator/) ist "die" Standard Implementierung
- Spring bietet eine einfache Integration [siehe hier](http://docs.spring.io/spring/docs/4.0.2.RELEASE/spring-framework-reference/htmlsingle/#validation-beanvalidation)
    1. Standard JSR-303 Annotationen
    2. Eigene Annotatione inkl. Validatoren
    3. Annotation-driven Validation 
    4. Multi-Level Validation möglioh
    5. Programmatischer Aufruf via @Autowired Validator
    6. Eigene @Validated Annotation mit zusätzlichen Features
- @SafeHtml benötigt jsoup-lib (keine peer-dependency?)
- @Validated groups -> keine Nutzung über @Valid
- Exception Handling:
    1. @Validated -> ConstraintViolationException (nur auf Klassenebene)
    2. @Validated (Methodenebene) und @Valid -> MethodArgumentNotValidException
    3. Unterschiedlicher Aufbau der Exceptions, aber aus beiden lassen die Objekte auslesen
- @Valid klappt eher suboptimal (partielle Arbeitsverweigerung)?
- @Valid wird aber für embedded Objects bebraucht, da @Validated nicht für Fields möglich ist
- @Valid wird von Spring nicht bei @RequestParam und @PathVariable berücksichtigt
- Validation benötigt zumindest teilweise einen SpringContext != Unit Tests?
- Apropo Tests... 

**Workflow:**

_Validation Trigger -> Spring PostProcessor -> Default Validator -> Validation -> Exception_    