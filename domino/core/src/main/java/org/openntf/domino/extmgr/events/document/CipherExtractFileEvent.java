/*
 * � Copyright IBM Corp. 2009,2010
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.openntf.domino.extmgr.events.document;

import org.openntf.domino.extmgr.EMBridgeEventParams;
import org.openntf.domino.extmgr.events.AbstractDocumentEvent;
import org.openntf.domino.extmgr.events.EMEventIds;

public class CipherExtractFileEvent extends AbstractDocumentEvent {
	private static EMBridgeEventParams[] params = { EMBridgeEventParams.SourceDbpath, EMBridgeEventParams.Noteid, EMBridgeEventParams.Flag,
			EMBridgeEventParams.Itemname, EMBridgeEventParams.Filename, EMBridgeEventParams.Username };

	@Override
	public EMBridgeEventParams[] getParams() {
		return params;
	}

	public CipherExtractFileEvent() {
		super(EMEventIds.EM_NSFNOTECIPHEREXTRACTFILE.getId());
	}

	public long getExtractFlags() {
		return (Long) getEventValuesMap().get(EMBridgeEventParams.Flag);
	}

	public String getItemName() {
		return (String) getEventValuesMap().get(EMBridgeEventParams.Itemname);
	}

	public String getFileName() {
		return (String) getEventValuesMap().get(EMBridgeEventParams.Filename);
	}

}