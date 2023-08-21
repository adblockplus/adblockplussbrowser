/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-2023 eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.adblockplussbrowser.telemetry.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.adblockplus.adblockplussbrowser.base.os.AppInfo
import org.adblockplus.adblockplussbrowser.base.os.CallingApp
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.telemetry.data.TelemetryRepository
import org.adblockplus.adblockplussbrowser.telemetry.reporters.ActivePingReporter
import org.adblockplus.adblockplussbrowser.telemetry.reporters.HttpReporter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TelemetryModule {

}