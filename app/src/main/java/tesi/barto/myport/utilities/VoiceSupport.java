package tesi.barto.myport.utilities;

/**
 * Created by Valentina on 29/08/2017.
 */

import android.content.Context;
import android.view.accessibility.AccessibilityManager;

public class VoiceSupport {

	public static boolean isTalkBackEnabled(Context context){
		AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
		boolean isAccessibilityEnabled = am.isEnabled();
		boolean isExploreByTouchEnabled = am.isTouchExplorationEnabled();
		return isExploreByTouchEnabled;
	}
}

