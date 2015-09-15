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

import groovy.transform.EqualsAndHashCode
import groovy.xml.MarkupBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import org.gradle.api.Plugin
import org.gradle.api.Project

import sagex.tools.gradle.plugins.sagetv.exceptions.InvalidManifestException

/**
 * Make life easier for the SageTV plugin developer
 * @author db
 *
 */
class SagePlugin implements Plugin<Project> {

	@EqualsAndHashCode(includes='type, value')
	private static class SageDependency {
		String type
		String value = ''
		String minVersion
		String maxVersion

		@Override
		String toString() {
			this.@value ?: this.@type
		}		
	}
	
	private static class SagePackage {
		String type
		URL location
		File file
		boolean overwrite
	}

	/**
	 * Plugin manifest XML is generated based on the contents of an instance of this class
	 */
	static class SageManifestExtension {
		/**
		 * Plugin name, must be unique amongst all registered plugins
		 */
		String name
		
		/**
		 * Plugin identifier; must be unique amongst all reigstered plugins
		 */
		String identifier
		
		/**
		 * Plugin description; can't be empty
		 */
		String description
		
		/**
		 * Author; should be forum user name, but can be anything you want
		 */
		String author
		
		/**
		 * Creation date: yyyy.mm.dd
		 */
		String created
		
		/**
		 * Last modified date: yyyy.mm.dd
		 * Defaults to current date if not provided
		 */
		String modified
		
		/**
		 * Plugin version: w.x.y[z...]; all numbers
		 */
		String version
		
		/**
		 * Set true if this plugin should be marked as a beta version
		 */
		boolean isBeta
		
		/**
		 * Resource path for plugin; relative paths are relative to user.dir
		 */
		String resourcePath
		
		/**
		 *  List of URLs linking to related web sites for this plugin
		 */
		List<URL> webPages = []
		
		/**
		 * True if this plugin can only be installed on SageTV server
		 */
		boolean isServerOnly
		
		/**
		 * True if this plugin requires a desktop environment
		 */
		boolean isDesktopRequired
		
		/**
		 * Type of plugin
		 */
		String pluginType
				
		/**
		 * If the plugin has an implementation class, specify it here
		 */
		String implementationClass
		
		/**
		 * Release notes for this version of the plugin
		 */
		String releaseNotes
	
		List<SagePackage> packages = []		
		List<URL> screenshots = []		
		List<URL> demoVideos = []		
		List<String> os = []		
		List<URL> stvImports = []		
		Set<SageDependency> dependencies = new HashSet<SageDependency>()

		private void addUniqueDep(SageDependency dep) {
			if(!dependencies.add(dep))
				throw new InvalidManifestException("Dependency defined multiple times: $dep")
		}	
		
		/**
		 * Add a plugin dependency for this plugin
		 * @param id The id of the plugin that must be installed before this one can be installed
		 * @param minVersion The minimum version of this dependency required; null if no min version requirement
		 * @param maxVersion The maximum version of this dependency allowed; null if no max version; this should be used sparingly, if ever
		 */
		void dependency(String id, String minVersion = null, String maxVersion = null) {
			def dep = new SageDependency(type: 'Plugin', value: id, minVersion: minVersion, maxVersion: maxVersion)
			addUniqueDep(dep)
		}
		
		/**
		 * Add a core dependency for this plugin
		 * @param minVersion The minimum version of the core required for this plugin to operate
		 * @param maxVersion The max version of the core allowed for this plugin; null if no max; use this sparingly, if ever
		 */
		void core(String minVersion, String maxVersion = null) {
			def dep = new SageDependency(type: 'Core', minVersion: minVersion, maxVersion: maxVersion)
			addUniqueDep(dep)
		}
		
		/**
		 * Add a JVM dependency for this plugin
		 * @param minVersion The min version of Java required for this plugin (1.5, 1.6, 1.7, 1.8, etc)
		 * @param maxVersion The max version of Java required for this plugin; optional; use sparingly, if ever
		 */
		void jvm(String minVersion, String maxVersion = null) {
			def dep = new SageDependency(type: 'JVM', minVersion: minVersion, maxVersion: maxVersion)
			addUniqueDep(dep)
		}
		
		/**
		 * Add OS dependencies for this plugin
		 * @param oses The list of operating systems this plugin supports
		 */
		void os(Object... oses) {
			oses.each { os << it.toString() }
		}
		
		/**
		 * Add an STVi package to this plugin
		 * @param urls A list of URLs pointing to STVi packages for this plugin
		 */
		void stvi(Object... urls) {
			urls.each {
				if(it instanceof URL)
					stvImports << it
				else
					stvImports << new URL(it.toString())
			}
		}
		
		/**
		 * Add a webpage link to the plugin info
		 * @param urls A list of URLs pointing to web sites for this plugin
		 */
		void webPage(Object... urls) {
			urls.each {
				if(it instanceof URL)
					webPages << it
				else
					webPages << new URL(it.toString())
			}
		}
		
		/**
		 * Add packages to this plugin
		 * <p>
		 *   Each argument is a Map consisting of the following keys:
		 *   <ul>
		 *     <li>type: The type of package, remember JAR is no longer supported!</li>
		 *     <li>location: A URL pointing to the location of this package file</li>
		 *     <li>file: A File object for this package; the md5 is computed from this file, it must exist and must the file pointed to by location</li>
		 *     <li>overwrite: Optional, true if not specified; when true this package overwrites any existing files contained in it on the Sage system</li>
		 * </p>
		 * @param pkgs A list of Maps, each containing the keys listed above
		 */
		void pkg(Map... pkgs) {
			pkgs.each {
				packages << new SagePackage(it)
			}
		}
		
		/**
		 * Add a screenshot to the plugin description
		 * @param urls A list of URLs pointing to screenshots
		 */
		void screenshot(Object... urls) {
			urls.each {
				if(it instanceof URL)
					screenshots << it
				else
					screenshots << new URL(it.toString())
			}
		}
		
		/**
		 * Add a demo URL to the plugin description
		 * @param urls A list of URLs pointing to demo videos
		 */
		void demo(Object... urls) {
			urls.each {
				if(it instanceof URL)
					demoVideos << it
				else
					demoVideos << new URL(it.toString())
			}
		}
	}

	/**
	 * Used for form submission to Sage plugin repo (for now)
	 */
	static class SagePluginDetailsExtension {
		String name
		String email
		String username
		String type
	}
	
	@Override
	void apply(Project proj) {
		proj.extensions.create('sageManifest', SageManifestExtension)
		proj.extensions.create('sagePluginDetails', SagePluginDetailsExtension)
		
		proj.task('manifest') {
			inputs.files { proj.configurations.runtime }
			outputs.file new File(proj.buildDir, 'sage-manifest/plugin.xml')
			doLast {
				setDefaults(proj, proj.sageManifest)
				validate(proj.sageManifest)
				mkManifest(proj, proj.sageManifest, it.outputs.files.singleFile)
			}
		}
		
		proj.task('submit') {
			inputs.files proj.manifest
			doLast {
				submit(proj.sageManifest, proj.sagePluginDetails, it.inputs.files.singleFile)
			}
		}
	}
	
	private void submit(SageManifestExtension manifest, SagePluginDetailsExtension plugin, File f) {
		println 'Waiting for package files to become available...'
		throw new RuntimeException('not implemented')
		def xml = new XmlSlurper().parse(f)
		xml.Package.each {
			print "\tChecking: $it.Location"
			def ready = false
			def start = System.currentTimeMillis()
			while(!ready && (System.currentTimeMillis() - start) < 60000) {
				def pkgHttp = new HTTPBuilder(it.Location)
				pkgHttp.request(Method.HEAD, ContentType.TEXT) { req ->
					response.success = { resp ->
						ready = true
					}
					response.failure = { resp ->
						print '.'
						sleep 5000
					}
				}
			}
			if(!ready)
				throw new RuntimeException("Package not available: $it.location")
			else
				println '. [PASS]'
		}
		println 'All packages found!'
		def http = new HTTPBuilder('http://download.sage.tv')
		def body = [
			Name: plugin.name,
			Email: plugin.email,
			Username: plugin.username,
			PluginID: manifest.identifier,
			RequestType: plugin.type,
			Manifest: f.text, 
		]
		http.post(path: '/pluginSubmit.php', body: body)
	}
	
	private void mkManifest(Project proj, SageManifestExtension input, File f) {
		f.delete()
		f.parentFile.mkdirs()
		f.withWriter {
			def ip = new IndentPrinter(it)
			def xml = new MarkupBuilder(ip)
			xml.SageTVPlugin {
				Name(input.name)
				Identifier(input.identifier ?: proj.name)
				def attrs = input.isBeta ? [beta: 'true'] : [:]
				Version(attrs, proj.version)
				Author(input.author)
				CreationDate(input.created)
				ModificationDate(input.modified)
				if(input.isServerOnly)
					ServerOnly('true')
				if(input.isDesktopRequired)
					Desktop('true')
				input.os.each {
					OS(it)
				}
				PluginType(input.pluginType)
				if(input.implementationClass)
					ImplementationClass(input.implementationClass)
				if(input.resourcePath)
					ResourcePath(input.resourcePath)
				input.dependencies.each { dep ->
					Dependency {
						if(dep.value)
							"$dep.type"(dep.value)
						else
							"$dep.type"()
						if(dep.minVersion)
							MinVersion(dep.minVersion)
						if(dep.maxVersion)
							MaxVersion(dep.maxVersion)						
					}
				}
				Jars {
					proj.configurations.runtime.allDependencies.findAll { it.group != 'sagetv' }.each { dep ->
						Jar("$dep.group:$dep.name:$dep.version")
					}
				}
				input.packages.each { pkg ->
					Package {
						PackageType(pkg.type)
						Location(pkg.location)
						def now = System.currentTimeMillis()
						proj.ant.checksum(file: pkg.file, property: "sage.md5.$now")
						MD5(proj.ant."sage.md5.$now")
						if(pkg.overwrite)
							Overwrite(true)
					}
				}
				input.stvImports.each {
					STVImport(it)
				}
				Description {
					mkp.yieldUnescaped "<![CDATA[\n$input.description\n"
					ip.printIndent()
					mkp.yieldUnescaped ']]>'
				}
				input.webPages.each {
					Webpage(it)
				}
				input.screenshots.each {
					Screenshot(it)
				}
				input.demoVideos.each {
					DemoVideo(it)
				}
				if(input.releaseNotes) {
					ReleaseNotes {
						mkp.yieldUnescaped "<![CDATA[\n$input.releaseNotes\n"
						ip.printIndent()
						mkp.yieldUnescaped ']]>'
					}
				}
			}
		}
	}
	
	private void validate(SageManifestExtension input) {
		def missing = []
		def invalid = []
		if(!input.name)
			missing << 'name'
		if(!input.identifier)
			missing << 'identifier'
		if(!input.version)
			missing << 'version'
		if(!input.description)
			missing << 'description'
		if(!input.author)
			missing << 'author'
		if(!input.pluginType)
			missing << 'pluginType'
		if(missing)
			throw new InvalidManifestException("Your sageManifest is missing required values: $missing")
			
		if(input.identifier.startsWith('-') || !(input.identifier ==~ /[-a-zA-Z0-9]+/))
			invalid << 'identifier'
		if(!(input.version ==~ /\d+(?:\.\d+)*/))
			invalid << 'version'
		if(!(input.pluginType ==~ /Standard|STVI?|Theme|Images|Library/))
			invalid << 'pluginType'
		if(input.created && !(input.created ==~ /\d{4}[.-]\d{2}[.-]\d{2}/))
			invalid << 'created'
		if(input.modified && !(input.modified ==~ /\d{4}[.-]\d{2}[.-]\d{2}/))
			invalid << 'modified'
		if(input.isDesktopRequired && input.isServerOnly)
			invalid << 'isDektopOnly & isServerOnly both can\'t be null'
		if(input.os && !(input.os ==~ /Windows|Linux|Macintosh/))
			invalid << 'os'
		if(invalid)
			throw new InvalidManifestException("Your sageManifest contains invalid values: $invalid")
	}
	
	private void setDefaults(Project proj, SageManifestExtension input) {
		input.identifier = input.identifier ?: proj.name
		input.description = input.description ?: proj.description
		input.version = input.version ?: proj.version
		input.modified = input.modified ?: new Date().format('yyyy.MM.dd')
	}
}
