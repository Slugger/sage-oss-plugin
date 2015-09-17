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
package sagex.oss.plugins.samples;

import java.util.Map;

import sage.SageTVPlugin;

public class SamplePlugin implements SageTVPlugin {

	public SamplePlugin() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void sageEvent(String arg0, Map arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getConfigHelpText(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConfigLabel(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getConfigOptions(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getConfigSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getConfigType(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getConfigValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getConfigValues(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetConfig() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfigValue(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfigValues(String arg0, String[] arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		System.out.println("The SageTV OSS sample plugin has been started!");
	}

	@Override
	public void stop() {
		System.out.println("The SageTV OSS sample plugin has been stopped!");
	}
}
