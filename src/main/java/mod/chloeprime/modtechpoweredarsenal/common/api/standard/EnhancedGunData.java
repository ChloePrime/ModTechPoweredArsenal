package mod.chloeprime.modtechpoweredarsenal.common.api.standard;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

public interface EnhancedGunData {
    default OptionalDouble getConfiguration(MtpaExtraConfiguration configuration) {
        return mtpa$getConfiguration()
                .map(cfg -> cfg.get(configuration.key()))
                .map(OptionalDouble::of)
                .orElse(OptionalDouble.empty());
    }

    Optional<Map<String, Double>> mtpa$getConfiguration();
}
