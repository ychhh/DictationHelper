package com.demo.common.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Generated by JFinal, do not modify this file.
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     _MappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class _MappingKit {
	
	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("tbl_book", "bid", TblBook.class);
		arp.addMapping("tbl_bookversion", "bvId", TblBookversion.class);
		arp.addMapping("tbl_grade", "git", TblGrade.class);
		arp.addMapping("tbl_unit", "unid", TblUnit.class);
		arp.addMapping("tbl_user", "uid", TblUser.class);
		arp.addMapping("tbl_word", "wid", TblWord.class);
	}
}

