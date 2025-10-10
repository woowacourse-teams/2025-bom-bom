# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**봄봄 (BomBom)** is a newsletter reader platform that helps users build reading habits by aggregating newsletters in one place and providing reading tracking features.

The project consists of:
- **Frontend Monorepo** (pnpm workspace): Web app, mobile app, and shared code
- **Backend**: Spring Boot application with Java 21, JPA, MySQL, and Spring Security
- **Mail Server**: Separate Spring Boot service for handling newsletter emails

## Development Commands

### Frontend Monorepo (pnpm workspace)

The frontend is organized as a **pnpm monorepo** with three packages:
- `web/` - React web application (TanStack Router, Emotion, TypeScript)
- `app/` - React Native mobile app (Expo)
- `shared/` - Shared code (theme, utilities, types)

```bash
cd frontend/

# Root-level commands (affects all workspaces)
pnpm install          # Install all dependencies
pnpm type-check       # Type-check all packages
pnpm lint             # Lint all packages
pnpm lint:fix         # Auto-fix lint issues
pnpm format           # Format all packages

# Web-specific commands
pnpm web:start        # Start web dev server on localhost:3000
pnpm web:build        # Production build
pnpm web:lint         # Lint web package
pnpm web:type-check   # Type-check web package
pnpm web:test         # Run Jest tests

# Mobile app commands
pnpm app:start        # Start Expo dev server
pnpm app:android      # Build and run Android app
pnpm app:ios          # Build and run iOS app

# Shared package commands
pnpm shared:build     # Build shared package
```

### Web Package Commands (cd frontend/web/)

```bash
# Development
npm run start          # Start dev server on localhost:3000
npm run start:msw      # Start dev server with MSW enabled
npm run build          # Production build
npm run test           # Run Jest tests

# Code Quality
npm run lint           # ESLint check
npm run lint:fix       # Auto-fix ESLint issues
npm run format         # Format with Prettier and Stylelint
npm run type-check     # TypeScript type checking

# E2E Testing
npm run test:e2e       # Run Playwright tests
npm run test:e2e:ui    # Run Playwright tests with UI
npm run test:e2e:headed # Run Playwright tests in headed mode
npm run test:e2e:debug # Debug Playwright tests

# Storybook
npm run storybook      # Start Storybook on port 6006
npm run build-storybook # Build Storybook

# API Types
npm run generate-openapi-types # Generate TypeScript types from OpenAPI spec
```

### Mobile App Commands (cd frontend/app/)

```bash
npm run start          # Start Expo dev server
npm run android        # Prebuild and run Android app
npm run ios            # Prebuild and run iOS app
npm run build:android  # Build Android app locally with EAS
npm run lint           # ESLint check
npm run format         # Format with Prettier
npm run type-check     # TypeScript type checking
```

### Backend (Spring Boot/Java)

```bash
cd backend/bom-bom-server/

# Development & Testing
./gradlew bootRun      # Start Spring Boot application
./gradlew test         # Run tests
./gradlew build        # Build application

# Database: MySQL for production, H2 for tests
```

## Architecture & Technology Stack

### Monorepo Structure

The frontend uses **pnpm workspaces** for monorepo management:

```
frontend/
├── pnpm-workspace.yaml    # Workspace configuration
├── package.json           # Root package with workspace scripts
├── web/                   # React web application
│   ├── src/               # Web app source code
│   └── package.json       # Web dependencies
├── app/                   # React Native mobile app (Expo)
│   ├── app/               # Expo Router file-based routing
│   └── package.json       # Mobile dependencies
└── shared/                # Shared code across web and mobile
    ├── src/
    │   ├── theme.ts       # Shared theme (Emotion)
    │   ├── webview.ts     # WebView bridge utilities
    │   └── index.ts       # Shared exports
    └── package.json       # Shared dependencies
```

**Key Benefits**:
- **Code Sharing**: Theme and utilities shared between web and mobile via `@bombom/shared`
- **Type Safety**: TypeScript types shared across packages
- **Dependency Management**: Centralized dependency management with pnpm
- **Consistent Tooling**: Shared ESLint, Prettier, and TypeScript configs

**Package Dependencies**:
- `web/` depends on `@bombom/shared` for theme and utilities
- `app/` depends on `@bombom/shared` for theme and utilities
- Changes to `shared/` automatically reflect in both web and mobile

### Web Package Architecture

- **Routing**: TanStack Router with file-based routing in `src/routes/`
- **Styling**: Emotion with styled-components pattern and centralized theme from `@bombom/shared`
- **State Management**: TanStack Query for server state
- **Testing**: Jest with Testing Library for unit tests, Playwright for E2E
- **Mocking**: MSW (Mock Service Worker) for API mocking
- **Build**: Webpack with Babel for TypeScript/React compilation

**Key Directories:**
- `src/components/` - Reusable UI components with Storybook stories
- `src/pages/` - Page-specific components and business logic
- `src/routes/` - TanStack Router route definitions
- `src/styles/` - Global styles and reset styles (theme in shared package)
- `src/mocks/` - MSW handlers and mock data
- `src/types/` - TypeScript type definitions
- `src/apis/` - API client functions and query definitions
- `src/libs/` - Third-party library integrations (Google Analytics, Channel Talk, WebView)
- `src/utils/` - Utility functions
- `e2e/` - Playwright E2E tests

### Mobile App Architecture

- **Framework**: Expo (React Native)
- **Routing**: Expo Router with file-based routing in `app/` directory
- **Styling**: Emotion Native with shared theme from `@bombom/shared`
- **Authentication**: Native Google Sign-In and Apple Authentication
- **Navigation**: React Navigation for tab and stack navigation
- **WebView**: React Native WebView for embedded content with bridge utilities from `@bombom/shared`

**Key Directories:**
- `app/` - Expo Router file-based routes
- `components/` - Reusable mobile UI components
- `constants/` - App-wide constants and configuration
- `hooks/` - Custom React hooks
- `assets/` - Images, fonts, and other static assets

### Backend Architecture

- **Framework**: Spring Boot 3.5.3 with Java 21
- **Database**: MySQL with JPA/Hibernate, QueryDSL for complex queries
- **Security**: Spring Security with OAuth2 (Google) authentication
- **Testing**: JUnit 5 with H2 in-memory database
- **Build**: Gradle with Kotlin DSL

**Key Packages:**
- `api.v1.*` - REST API controllers and DTOs organized by domain
- `domain.*` - JPA entities and domain models
- `repository.*` - Data access layer with custom QueryDSL implementations
- `service.*` - Business logic layer
- `config.*` - Spring configuration classes

### Shared Package Architecture

The `shared/` package contains code shared between web and mobile:

**Exports:**
- `@bombom/shared` - Main exports (index.ts)
- `@bombom/shared/theme` - Emotion theme for consistent styling
- `@bombom/shared/webview` - WebView bridge utilities for mobile-web communication

**Usage in Web:**
```typescript
import { theme } from '@bombom/shared/theme';
import { someUtil } from '@bombom/shared';
```

**Usage in Mobile:**
```typescript
import { theme } from '@bombom/shared/theme';
import { webviewBridge } from '@bombom/shared/webview';
```

## Project Conventions

### Git Workflow

- **Commit Format**: `type: subject` (see docs/CONVENTION.md)
  - Types: `feat`, `fix`, `hotfix`, `refactor`, `style`, `docs`, `test`, `chore`
- **PR Format**: `[{scope}][{issue_key}] {type}:{subject}`
  - Scope: FE (Frontend), BE (Backend)
  - Example: `[FE][BOM-75] feat: SearchInput component 제작`
- **Branch Naming**: `{type}/{issue_key}` (e.g., `feat/BOM-5`)
- **Merge Strategy**: Squash & Merge to develop, Create a Commit merge to main

### Code Style

- **Frontend**: ESLint + Prettier with TypeScript strict mode
- **Backend**: Standard Java conventions with Lombok
- **File Naming**: PascalCase for components, camelCase for utilities
- **Import Order**: External libraries, internal modules, relative imports

### Design System

- **Spacing**: Use 4px units (4px, 8px, 12px, 16px, etc.)
- **Theme**: Centralized theme in `@bombom/shared/theme` shared across web and mobile
- **Components**: All components include Storybook stories (web) or documentation (mobile)
- **TypeScript**: Strict mode enabled with interfaces for all props

## Monorepo Development Workflow

### Adding Dependencies

**Add to specific package:**
```bash
# Add to web package
pnpm --filter web add react-hook-form

# Add to app package
pnpm --filter app add expo-camera

# Add to shared package
pnpm --filter shared add lodash
```

**Add dev dependency to root:**
```bash
pnpm add -Dw prettier
```

### Working Across Packages

When making changes to `shared/` package:
1. Make changes in `shared/src/`
2. Changes are automatically available in `web/` and `app/` (TypeScript paths)
3. No build step required for development (direct source imports)
4. For production builds, `shared/` may need explicit build step

### Cross-Package Imports

Web and mobile packages import from shared package:

```typescript
// In web/src/App.tsx or app/App.tsx
import { theme } from '@bombom/shared/theme';
import { useWebViewBridge } from '@bombom/shared/webview';
```

TypeScript path aliases are configured in each package's tsconfig.json.

## Key Technical Implementation Details

### File-Based Routing (Web)

TanStack Router uses file-based routing with auto-generated route tree:

- Route files in `web/src/routes/` automatically generate `routeTree.gen.ts`
- Layout routes use underscore prefix: `_bombom.tsx` creates a layout
- Nested routes go in folders: `_bombom/index.tsx` for nested content
- Dynamic routes use `$` notation: `articles.$articleId.tsx`
- Don't manually edit `routeTree.gen.ts` - it's auto-generated by TanStack Router plugin

### File-Based Routing (Mobile)

Expo Router uses file-based routing in `app/` directory:

- Route files in `app/` directory automatically generate navigation structure
- Layout routes use `_layout.tsx` files
- Index routes use `index.tsx` files
- Dynamic routes use `[param].tsx` notation
- Tab navigation defined in `(tabs)/` directory structure

### API Integration Pattern (Web)

The web package uses a centralized query definition pattern:

- All TanStack Query definitions are exported from `web/src/apis/queries.ts`
- Query keys are consistently structured: `['resource', 'subresource', params]`
- Example: `queries.articleById({ id: '123' })`, `queries.infiniteArticles({ status: 'unread' })`
- Custom hooks in `web/src/pages/*/hooks/` consume these query definitions
- Mutations are defined in page-specific hooks (e.g., `useAddBookmarkMutation`)

### MSW (Mock Service Worker) - Web Only

MSW is conditionally enabled via environment variable for web development:

- Start with MSW: `npm run start:msw` (sets `ENABLE_MSW=true`)
- Handlers are defined in `web/src/mocks/handlers.ts` and `web/src/mocks/handlers/`
- Mock data is stored in `web/src/mocks/datas/`
- Service worker file is located at `web/public/mockServiceWorker.js`
- MSW initialization happens in `web/src/main.tsx` before React renders

### WebView Bridge (Mobile-Web Communication)

The shared package provides WebView bridge utilities for mobile-web communication:

- Bridge utilities in `shared/src/webview.ts`
- Mobile app uses React Native WebView to embed web content
- Web app detects WebView context and uses bridge for native features
- Supports bidirectional communication for features like auth, navigation, and notifications

## Development Tips

### Monorepo Tips

- **Install once**: Run `pnpm install` at root to install all dependencies
- **Workspace commands**: Use `pnpm --filter <package>` to run commands in specific packages
- **Type checking**: Run `pnpm type-check` at root to check all packages
- **Shared changes**: Changes in `shared/` are immediately available in `web/` and `app/`

### Web Development

- Use the theme object for consistent colors and typography: `theme.colors.primary`, `theme.fonts.heading1`
- All components should include TypeScript interfaces and Storybook stories
- MSW is configured for API mocking - add new handlers in `src/mocks/handlers.ts`
- Router devtools are available in development mode
- Use centralized query definitions from `src/apis/queries.ts` instead of inline queries

### Mobile Development

- Use Expo dev client for custom native modules
- Test on both iOS and Android platforms
- Use shared theme from `@bombom/shared/theme` for consistent styling
- WebView bridge utilities available from `@bombom/shared/webview`
- Prebuild step required for native builds: `npm run android` or `npm run ios`

### Backend Development

- H2 database is used for testing, MySQL for production
- Spring Security is configured with OAuth2 Google authentication
- Lombok is available for reducing boilerplate code
- QueryDSL is used for complex queries - generated Q-classes are in `src/main/generated`
- Swagger UI is available at `/swagger-ui.html` in development

## Testing Strategy

### Web Testing

- **Unit Tests**: Jest with Testing Library in `web/src/`
  - Component tests
  - Utility function tests
  - Hook tests
- **E2E Tests**: Playwright in `web/e2e/`
  - Full user workflow tests
  - Cross-browser testing
  - Auto-starts dev server for testing

### Mobile Testing

- Manual testing on iOS and Android devices/simulators
- Expo dev client for testing native modules

### Backend Testing

- JUnit 5 with H2 in-memory database
- Integration tests for repositories and services
- Controller tests with MockMvc

## Key Features Implementation

### Core Newsletter Features

- **Reading Progress Tracking**: Track reading progress per newsletter
- **Social Features**: Follow other users, leaderboards for motivation
- **Highlight & Notes**: Save important sections with personal notes
- **Archive System**: Knowledge management from collected notes

### Cross-Platform Features

- **Web**: Full-featured desktop and mobile web experience
- **Mobile**: Native mobile app with WebView embedding for web content
- **Shared**: Consistent theme, styling, and utilities across platforms

This is an active project focused on building reading habits through newsletter aggregation and social reading features.
