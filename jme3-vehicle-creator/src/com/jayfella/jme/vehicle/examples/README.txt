Example-vehicle code vendored from the MavCommon subproject of the
jme-vehicles project: https://github.com/stephengold/jme-vehicles

Source revision: tag library-v0.7.2
License: BSD 3-clause, see license.txt in this directory.

Files copied (unmodified unless noted):
  engines/FlexibleEngine.java
  sounds/EngineSound4.java
  sounds/HornSound1.java
  tires/Tire02.java
  vehicles/HatchBack.java  (driver/passenger removed; its assets
                            are not vendored into the SDK)
  wheels/HatchbackWheel.java

The corresponding assets live under the module's src/Models folder:
  Models/modern_hatchback/  (chassis, collision shape, wheel, textures)
  Audio/                    (engine and horn sounds)

MavCommon is not published to Maven Central, which is why the sources
and assets are vendored here instead of added as a library dependency.
