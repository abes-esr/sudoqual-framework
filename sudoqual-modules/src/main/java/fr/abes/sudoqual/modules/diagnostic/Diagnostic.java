package fr.abes.sudoqual.modules.diagnostic;

enum Diagnostic {

    CASE_INVALID(Status.INVALID_DIAGNOSTIC_CODE), // 0
    CASE_1(Status.VALIDATED_LINK), // 1
    CASE_2(Status.ERRONEOUS_LINK), // 2
    CASE_3(Status.ERRONEOUS_LINK), // ...
    CASE_4(Status.ERRONEOUS_LINK),
    CASE_5(Status.ERRONEOUS_LINK),
    CASE_6(Status.ALMOST_VALIDATED_LINK),
    CASE_7(Status.DOUBTFUL_LINK),
    CASE_8(Status.DOUBTFUL_LINK),
    CASE_9(Status.MISSING_LINK),
    CASE_10(Status.MISSING_LINK),
    CASE_11(Status.MISSING_LINK),
    CASE_12(Status.MISSING_LINK); // 12

    public enum Status {
        INVALID_DIAGNOSTIC_CODE,
        VALIDATED_LINK,
        ERRONEOUS_LINK,
        ALMOST_VALIDATED_LINK,
        DOUBTFUL_LINK,
        MISSING_LINK;

        @Override
        public String toString() {
            switch(this) {
            case VALIDATED_LINK:
                return "validatedLink";
            case ERRONEOUS_LINK:
                return "erroneousLink";
            case ALMOST_VALIDATED_LINK:
                return "almostValidatedLink";
            case DOUBTFUL_LINK:
                return "doubtfulLink";
            case MISSING_LINK:
                return "missingLink";
            default:
                return "invalidDiagnosticCode";
            }
        }
    }

    private Status status;

    Diagnostic(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }
}
