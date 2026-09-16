package in.techgeneza.medycatalog.modules.billing.domain;

public final class AdsPlacementPolicy {

    private AdsPlacementPolicy() {
    }

    public static boolean allowedOnStudentUi(String placement) {
        if (placement == null || placement.isBlank()) {
            return false;
        }
        String value = placement.toUpperCase();
        return !value.contains("PLAYER") && !value.contains("EXAM") && !value.contains("SUBMIT");
    }
}
