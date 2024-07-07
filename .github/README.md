# GitHub Actions

## [Release Build](workflows/release-build.yml)

This workflow allows you to manually trigger a new release. You can set the new version using the **input field** in the [RELEASE Build](https://github.com/remsfal/remsfal-backend/actions/workflows/release-build.yml) Action.

![Screenshot of version input](https://github.com/remsfal/remsfal-backend/assets/54059879/91bac827-28aa-4129-9570-54b999371bd6)  
_Screenshot of version input field_

If no version is set, the current version will be incremented by a patch (X.X.1 -> X.X.2).

The pipeline performs the following tasks:
- Creates a new commit with the updated POM.xml files
- Tags the commit with the specified version
- Pushes the build packages to the [GitHub Package Registry](https://github.com/remsfal/remsfal-backend/packages/)

The created [release](https://github.com/remsfal/remsfal-backend/releases) will contain the current source code and the `remsfal-service-runner.jar`.