# Changelog

All notable changes to OMLib. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Fixed

- Forge 1.20.1: the common item tags (`c:iron_ingots`, `c:gold_ingots`, `c:redstone_dusts`, `c:quartz`, `c:diamonds`, `c:glass_panes`) are defined from Forge's own tags; Forge has none, so recipes using them matched no items
