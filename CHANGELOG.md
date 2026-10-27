# Changelog

All notable changes to OMLib. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- Containers whose slots hold more than one stack (`LargeStackContainer`, with menu slots, menu and screen): saved, synced and moved by hoppers and pipes on all loaders; counts from four digits on are drawn smaller, the tooltip shows count and limit

### Fixed

- Forge 1.20.1: the common item tags (`c:iron_ingots`, `c:gold_ingots`, `c:redstone_dusts`, `c:quartz`, `c:diamonds`, `c:glass_panes`) are defined from Forge's own tags; Forge has none, so recipes using them matched no items
