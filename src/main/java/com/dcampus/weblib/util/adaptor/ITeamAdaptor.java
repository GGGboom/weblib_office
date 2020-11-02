package com.dcampus.weblib.util.adaptor;

import java.io.InputStream;
import java.io.OutputStream;

public interface ITeamAdaptor {

	/**
	 * 导入用户组
	 *
	 * @param is
	 */
	void importTeam(InputStream is);

	/**
	 * 导出用户组
	 *
	 * @param os
	 */
	void exportTeam(OutputStream os);

}
