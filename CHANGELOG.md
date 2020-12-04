# Changelog

## [Unreleased](https://github.com/entigolabs/entigo-pipeline-plugin/compare/v10...HEAD)

## [v10](https://github.com/entigolabs/entigo-pipeline-plugin/tree/v10) (2020-12-04)

[Full Changelog](https://github.com/entigolabs/entigo-pipeline-plugin/compare/v9...v10)

### Added
- Unified retry process with timeout for ArgoCD requests [\#7](https://github.com/entigolabs/entigo-pipeline-plugin/pull/7)
    - Sync request now retries when another operation is in progress

## [v9](https://github.com/entigolabs/entigo-pipeline-plugin/tree/v9) (2020-11-25)

[Full Changelog](https://github.com/entigolabs/entigo-pipeline-plugin/compare/v8...v9)

### Added
- Flag for setting if sync wait timeout should fail the build or not [\#6](https://github.com/entigolabs/entigo-pipeline-plugin/pull/6)

## [v8](https://github.com/entigolabs/entigo-pipeline-plugin/tree/v8) (2020-11-19)

[Full Changelog](https://github.com/entigolabs/entigo-pipeline-plugin/compare/v5...v8)

### Added
- listArgoConnections step [\#5](https://github.com/entigolabs/entigo-pipeline-plugin/pull/5)
- deleteArgoApp step [\#4](https://github.com/entigolabs/entigo-pipeline-plugin/pull/4)
- getArgoApp step [\#3](https://github.com/entigolabs/entigo-pipeline-plugin/pull/3)

## [v5](https://github.com/entigolabs/entigo-pipeline-plugin/tree/v5) (2020-11-18)

[Full Changelog](https://github.com/entigolabs/entigo-pipeline-plugin/compare/7c2106336b9014c79ed1dcef8540db67364f3b7a...v5)

### Initial release
- syncArgoApp step
- support for multiple ArgoCD connections
- ArgoCD connection matchers