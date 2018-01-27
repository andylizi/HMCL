/*
 * Hello Minecraft! Launcher.
 * Copyright (C) 2017  huangyuhui <huanghongxun2008@126.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */
package org.jackhuang.hmcl.ui.construct;

import com.jfoenix.concurrency.JFXUtilities;
import javafx.beans.property.StringProperty;
import org.jackhuang.hmcl.Main;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.task.TaskExecutor;
import org.jackhuang.hmcl.task.TaskListener;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.wizard.AbstractWizardDisplayer;
import org.jackhuang.hmcl.util.OperatingSystem;
import org.jackhuang.hmcl.util.StringUtils;

import java.util.Map;

public interface TaskExecutorDialogWizardDisplayer extends AbstractWizardDisplayer {

    @Override
    default void handleTask(Map<String, Object> settings, Task task) {
        TaskExecutorDialogPane pane = new TaskExecutorDialogPane(() -> {
            Controllers.closeDialog();
            Controllers.navigate(null);
        });

        pane.setCurrentState(Main.i18n("message.doing"));
        pane.setProgress(Double.MAX_VALUE);
        if (settings.containsKey("title")) {
            Object title = settings.get("title");
            if (title instanceof StringProperty)
                pane.currentStateProperty().bind((StringProperty) title);
            else if (title instanceof String)
                pane.setCurrentState((String) title);
        }

        if (settings.containsKey("subtitle")) {
            Object subtitle = settings.get("subtitle");
            if (subtitle instanceof StringProperty)
                pane.stepsProperty().bind((StringProperty) subtitle);
            else if (subtitle instanceof String)
                pane.setSteps((String) subtitle);
        }

        JFXUtilities.runInFX(() -> {
            TaskExecutor executor = task.executor(e -> new TaskListener() {
                @Override
                public void onSucceed() {
                    if (settings.containsKey("success_message") && settings.get("success_message") instanceof String)
                        JFXUtilities.runInFX(() -> Controllers.dialog((String) settings.get("success_message"), null, MessageBox.FINE_MESSAGE, () -> Controllers.navigate(null)));
                    else if (!settings.containsKey("forbid_success_message"))
                        JFXUtilities.runInFX(() -> Controllers.dialog(Main.i18n("message.success"), null, MessageBox.FINE_MESSAGE, () -> Controllers.navigate(null)));
                }

                @Override
                public void onTerminate() {
                    String appendix = StringUtils.getStackTrace(e.getLastException());
                    if (settings.containsKey("failure_message") && settings.get("failure_message") instanceof String)
                        JFXUtilities.runInFX(() -> Controllers.dialog(appendix, (String) settings.get("failure_message"), MessageBox.ERROR_MESSAGE, () -> Controllers.navigate(null)));
                    else if (!settings.containsKey("forbid_failure_message"))
                        JFXUtilities.runInFX(() -> Controllers.dialog(appendix, Main.i18n("wizard.failed"), MessageBox.ERROR_MESSAGE, () -> Controllers.navigate(null)));
                }
            });
            pane.setExecutor(executor);
            Controllers.dialog(pane);
            executor.start();
        });
    }
}