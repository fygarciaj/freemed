<?php
 // $Id$
 // note: cpt modifier functions
 // lic : GPL, v2

LoadObjectDependency('FreeMED.MaintenanceModule');

class CptModifiersMaintenance extends MaintenanceModule {

	var $MODULE_NAME    = "CPT Modifiers Maintenance";
	var $MODULE_AUTHOR  = "jeff b (jeff@ourexchange.net)";
	var $MODULE_VERSION = "0.1.1";
	var $MODULE_FILE    = __FILE__;

	var $PACKAGE_MINIMUM_VERSION = '0.6.0';

	var $record_name    = "CPT Modifiers";
	var $table_name     = "cptmod";

	var $variables = array (
		"cptmod",
		"cptmoddescrip"
	);

	function CptModifiersMaintenance () {
		global $display_buffer;

			// table definition (inside constructor, as outside definitions
			// do NOT allow function calls)
		$this->table_definition = array (
			"cptmod"		=>	SQL_CHAR(2),
			"cptmoddescrip"		=>	SQL_VARCHAR(50),
			"id"			=>	SQL_NOT_NULL(SQL_AUTO_INCREMENT(SQL_INT(0)))
		);
		if ($debug) {
		global $sql;$display_buffer .= "query = \"".$sql->create_table_query(
			$this->table_name, $this->table_definition).
			"\"<BR>\n";
		} // end if $debug

			// Run constructor
		$this->MaintenanceModule();
	} // end constructor CptModifiersMaintenance

	function form () {
		global $display_buffer;
		foreach ($GLOBALS AS $k => $v) { global ${$k}; }
		switch ($action) { // inner switch
			case "addform":
			break;

			case "modform":
			if ($id<1) trigger_error ("NO ID", E_USER_ERROR);
			$r = freemed::get_link_rec ($id, $this->table_name);
			extract ($r);
			break;
		} // end inner switch

		$display_buffer .= "
		<p/>
		<form ACTION=\"$this->page_name\" METHOD=\"POST\">
		<input TYPE=\"HIDDEN\" NAME=\"action\" VALUE=\"".
		( ($action=="addform") ? "add" : "mod" )."\"/> 
		<input TYPE=\"HIDDEN\" NAME=\"id\"   VALUE=\"".prepare($id)."\"/>
		<input TYPE=\"HIDDEN\" NAME=\"module\"   VALUE=\"".prepare($module)."\"/>
		".html_form::form_table ( array (
		_("Modifier") =>
		array(
			'content' => html_form::text_widget('cptmod', 2)	
		),

		_("Description") =>
		array(
		    'help' => _("Helpful description of the modifier"),
		    'content' => html_form::text_widget('cptmoddescrip', 20, 30)
		)
		) )."
		<p/>
		<div ALIGN=\"CENTER\">
		<input TYPE=SUBMIT VALUE=\" ".
		( ($action=="addform") ? _("Add") : _("Modify") )." \"/>
		<input TYPE=\"SUBMIT\" VALUE=\""._("Cancel")."\"/>
		</div></form>
		";
	} // end function CptModifiersMaintenance->form()

	function view () {
		global $display_buffer;
		global $sql;
		$display_buffer .= freemed_display_itemlist (
			$sql->query (
				"SELECT cptmod,cptmoddescrip,id ".
				"FROM ".addslashes($this->table_name)." ".
				freemed::itemlist_conditions().
                		"ORDER BY cptmod,cptmoddescrip"
			),
			$this->page_name,
			array (
				_("Modifier")		=>	"cptmod",
				_("Description")	=>	"cptmoddescrip"
			),
			array ("", _("NO DESCRIPTION"))
		);
	} // end function CptModifiersMaintenance->view()

} // end class CptModifiersMaintenance

register_module ("CptModifiersMaintenance");

?>
