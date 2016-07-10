package ru.tehkode.permissions.bukkit;

import java.io.File;



public class ErrorReportBuilder {

    private final String name;
    private final StringBuilder message = new StringBuilder();
    private final Throwable error;

    ErrorReportBuilder(String name, Throwable error) {
        this.name = name;
        this.error = error;
    }

    public ErrorReportBuilder addHeading(String text) {
        message.append("### ").append(text).append(" ###\n");
        return this;
    }

    public ErrorReportBuilder addText(String text) {
        message.append('\n').append(text).append('\n');
        return this;
    }

    public ErrorReportBuilder addCode(String text, String format) {
        message.append("```");
        if (format != null) {
            message.append(format);
        }
        message.append('\n').append(text).append("\n```\n");
        return this;
    }

    public ErrorReport build() {
        ErrorReportBuilder builder = new ErrorReportBuilder(name, error);
        builder.addHeading("Description").addText("[Insert description of issue here]");
        builder.addHeading("Detailed Information");
        if (new File("plugins" + File.separator + "PermissionsEx", "report-disable").exists()) {
            builder.addText("I am stupid and chose to disable error reporting, therefore removing any chance of getting help with my error");
        } else {
            builder.addText("[Is available here](" + ErrorReport.gistText(this.message.toString()) + ")");
        }
        return new ErrorReport(this.name, builder.message.toString(), error);
    }
}

