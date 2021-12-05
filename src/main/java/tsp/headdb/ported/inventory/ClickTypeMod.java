package tsp.headdb.ported.inventory;

public enum ClickTypeMod {
	LEFT,
	LEFT_SHIFT,
	RIGHT,
	RIGHT_SHIFT;
	
	public static ClickTypeMod get(boolean right, boolean shift) {
		if (right) {
			if (shift)
				return RIGHT_SHIFT;
			else
				return RIGHT;
		} else {
			if (shift)
				return LEFT_SHIFT;
			else
				return LEFT;
		}
	}
}
