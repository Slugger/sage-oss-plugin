/*
 *      Copyright 2015 Battams, Derek
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package sagex.tools.gradle.plugins.sagetv

import org.gradle.api.Project

import sagex.tools.gradle.plugins.sagetv.SagePlugin.SageManifestExtension
import spock.lang.Specification
import spock.lang.Unroll

class SagePluginTests extends Specification {

	@Unroll
	def '#field defaults to non-null value'() {
		setup:
			Project proj = Mock()
			proj./.+/ >> 'abc'
			def manifest = new SageManifestExtension()
		when: 'a field with a default is not submitted'
			assert manifest."$field" == null
			new SagePlugin().setDefaults(proj, manifest)
		then: 'it is defaulted to a non-null'
			manifest."$field" != null
		where:
			field << ['identifier', 'description', 'version', 'modified']
	}
}
