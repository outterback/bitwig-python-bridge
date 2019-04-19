package com.outterback;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class PythonBridgeExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("6799e026-e0ab-4e99-9ee7-81acaf50c0a1");
   
   public PythonBridgeExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "Python Bridge";
   }
   
   @Override
   public String getAuthor()
   {
      return "Oscar Utterback";
   }

   @Override
   public String getVersion()
   {
      return "0.1";
   }

   @Override
   public UUID getId()
   {
      return DRIVER_ID;
   }
   
   @Override
   public String getHardwareVendor()
   {
      return "outterback";
   }
   
   @Override
   public String getHardwareModel()
   {
      return "Python Bridge";
   }

   @Override
   public int getRequiredAPIVersion()
   {
      return 8;
   }

   @Override
   public int getNumMidiInPorts()
   {
      return 0;
   }

   @Override
   public int getNumMidiOutPorts()
   {
      return 0;
   }

   @Override
   public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list, final PlatformType platformType)
   {
   }

   @Override
   public PythonBridgeExtension createInstance(final ControllerHost host)
   {
      return new PythonBridgeExtension(this, host);
   }
}
