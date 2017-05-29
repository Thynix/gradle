/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.tasks.diagnostics;

import org.gradle.TaskExecutionRequest;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionListener;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.tasks.TaskState;
import org.gradle.api.tasks.diagnostics.internal.DryRunReportRenderer;
import org.gradle.api.tasks.diagnostics.internal.ReportRenderer;
import org.gradle.execution.TaskGraphExecuter;
import org.gradle.execution.TaskSelector;
import org.gradle.execution.commandline.CommandLineTaskParser;
import org.gradle.execution.taskgraph.DefaultTaskExecutionPlan;
import org.gradle.execution.taskgraph.TaskExecutionPlan;
import org.gradle.internal.resources.ResourceLockCoordinationService;
import org.gradle.internal.work.WorkerLeaseService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class DryRunReportTask extends AbstractReportTask {
    private final DryRunReportRenderer renderer = new DryRunReportRenderer();
    private final CommandLineTaskParser commandLineTaskParser;
    private final ResourceLockCoordinationService coordinationService;
    private final WorkerLeaseService workerLeaseService;

    @Inject
    public DryRunReportTask(CommandLineTaskParser commandLineTaskParser, ResourceLockCoordinationService coordinationService, WorkerLeaseService workerLeaseService) {
        this.commandLineTaskParser = commandLineTaskParser;
        this.coordinationService = coordinationService;
        this.workerLeaseService = workerLeaseService;
    }

    @Override
    protected ReportRenderer getRenderer() {
        return renderer;
    }

    @Override
    protected void generate(Project project) throws IOException {
        DefaultTaskExecutionPlan taskExecutionPlan = new DefaultTaskExecutionPlan(null, coordinationService, workerLeaseService);

        ProjectInternal projectInternal = (ProjectInternal) project;
        GradleInternal gradle = projectInternal.getGradle();

        List<TaskExecutionRequest> taskParameters = gradle.getStartParameter().getTaskRequests();
        for (TaskExecutionRequest taskParameter : taskParameters) {
            List<TaskSelector.TaskSelection> taskSelections = commandLineTaskParser.parseTasks(taskParameter);
            for (TaskSelector.TaskSelection taskSelection : taskSelections) {
                getLogger().info("Selected primary task '{}' from project {}", taskSelection.getTaskName(), taskSelection.getProjectPath());
                taskExecutionPlan.addToTaskGraph(taskSelection.getTasks());
            }
        }

        taskExecutionPlan.determineExecutionPlan();

        for (Task task : taskExecutionPlan.getTasks()) {
            renderer.addTask(task);
        }
    }
}
