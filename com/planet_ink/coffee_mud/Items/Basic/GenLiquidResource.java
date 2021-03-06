package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2017 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class GenLiquidResource extends GenDrink implements RawMaterial, Drink
{
	@Override
	public String ID()
	{
		return "GenLiquidResource";
	}

	public GenLiquidResource()
	{
		super();
		setName("a puddle of resource thing");
		setDisplayText("a puddle of resource sits here.");
		setDescription("");
		setMaterial(RawMaterial.RESOURCE_FRESHWATER);
		disappearsAfterDrinking=false;
		basePhyStats().setWeight(0);
		setCapacity(0);
		recoverPhyStats();
	}

	protected static Ability rot=null;

	@Override
	public void setMaterial(int newValue)
	{
		super.setMaterial(newValue);
		decayTime=0;
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(rot==null)
		{
			rot=CMClass.getAbility("Prayer_Rot");
			if(rot==null)
				return;
			rot.setAffectedOne(null);
		}
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.FOODROT))
			rot.executeMsg(this,msg);
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(rot==null)
		{
			rot=CMClass.getAbility("Prayer_Rot");
			if(rot==null)
				return true;
			rot.setAffectedOne(null);
		}
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.FOODROT))
		{
			if(!rot.okMessage(this,msg))
				return false;
		}
		return super.okMessage(host,msg);
	}

	protected String	domainSource	= null;

	@Override
	public String domainSource()
	{
		return domainSource;
	}

	@Override
	public void setDomainSource(String src)
	{
		domainSource = src;
	}

	@Override
	public boolean rebundle()
	{
		return false;
	}// CMLib.materials().rebundle(this);}

	@Override
	public void quickDestroy()
	{
		CMLib.materials().quickDestroy(this);
	}
}
