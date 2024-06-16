/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.injection.accessor;

import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Provides the means to access protected members of the Abuse Report Context.
 */
@Mixin(AbuseReportContext.class)
public interface AbuseReportContextAccessor {
    /**
     * Returns the reporter environment.
     *
     * @return environment
     */
    @Accessor
    ReporterEnvironment getEnvironment();
}
