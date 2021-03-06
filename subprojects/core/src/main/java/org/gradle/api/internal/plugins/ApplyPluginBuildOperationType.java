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

package org.gradle.api.internal.plugins;

import org.gradle.api.Nullable;
import org.gradle.internal.operations.BuildOperationType;
import org.gradle.internal.scan.UsedByScanPlugin;

/**
 * Details about a plugin being applied.
 *
 * @since 4.0
 */
public final class ApplyPluginBuildOperationType implements BuildOperationType<ApplyPluginBuildOperationType.Details, Void> {

    @UsedByScanPlugin
    public interface Details {

        /**
         * The fully qualified plugin ID, if known.
         */
        @Nullable
        String getPluginId();

        /**
         * The fully qualified class name of the Plugin implementation.
         */
        String getClassName();

        /**
         * The target of the plugin.
         * One of "gradle", "settings", "project" or null.
         */
        String getTargetType();

        /**
         * If the target is a project, its path.
         */
        @Nullable
        String getTargetPath();

        /**
         * The build path of the target.
         */
        String getBuildPath();
    }

    private ApplyPluginBuildOperationType() {
    }
}
