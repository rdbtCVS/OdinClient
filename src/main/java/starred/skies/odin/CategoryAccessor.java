package starred.skies.odin;

import com.odtheking.odin.features.Category;

public interface CategoryAccessor {
    Category registerCategory(String name, String displayName);
}