package ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro.model;

public enum Weekday {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    UNKNOWN;

    public static Weekday getWeekday(int dayNumber) {
        return switch (dayNumber) {
            case 1 -> Weekday.MONDAY;
            case 2 -> Weekday.TUESDAY;
            case 3 -> Weekday.WEDNESDAY;
            case 4 -> Weekday.THURSDAY;
            case 5 -> Weekday.FRIDAY;
            default -> Weekday.UNKNOWN;
        };
    }
}