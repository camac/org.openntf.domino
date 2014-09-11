package org.openntf.domino.nsfdata.structs.cd;

import java.nio.ByteBuffer;

import org.openntf.domino.nsfdata.structs.SIG;

/**
 * This structure is placed at the start of each paragraph in a rich-text field, and specifies which CDPABDEFINITION is used as the format
 * for the paragraph. (editods.h)
 * 
 * @author jgallagher
 *
 */
public class CDPABREFERENCE extends CDRecord {

	protected CDPABREFERENCE(final SIG signature, final ByteBuffer data) {
		super(signature, data);
	}

	/**
	 * @return ID number of the CDPABDEFINITION used by this paragraph
	 */
	public short getPabId() {
		return getData().getShort(getData().position() + 0);
	}

	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + ", PABID: " + getPabId() + "]";
	}
}