package com.portal.entity;

public class Views {
	public static class List {
	};

	public static class Thin {
	};

	public static class Summary extends Thin {
	};

	public static class SummaryPlus extends Summary {
	};

	public static class SummaryDeep extends SummaryPlus {
	};

	public static class Detailed extends SummaryDeep {
	};

	public static class Full extends Detailed {
	};
}
