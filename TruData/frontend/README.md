# TruData Frontend

Modern React frontend for the TruData platform with TypeScript and Tailwind CSS.

## Technology Stack

- **React**: 18.2
- **TypeScript**: 5.2
- **Vite**: 5.0 (Build tool)
- **Tailwind CSS**: 3.3 (Styling)
- **Axios**: HTTP client
- **React Dropzone**: File upload
- **Lucide React**: Icons

## Project Structure

```
frontend/
├── src/
│   ├── components/              # React components
│   │   ├── Header.tsx          # App header
│   │   ├── FileUpload.tsx      # File upload interface
│   │   ├── LoadingSpinner.tsx  # Loading state
│   │   ├── AnalysisResults.tsx # Results container
│   │   ├── HealthScoreCard.tsx # Health score display
│   │   ├── MetricsGrid.tsx     # Quality metrics grid
│   │   ├── IssuesList.tsx      # Issues display
│   │   ├── ColumnProfilesTable.tsx  # Column details
│   │   └── RecommendationsList.tsx  # Recommendations
│   ├── services/
│   │   └── api.ts              # API client
│   ├── types/
│   │   └── index.ts            # TypeScript types
│   ├── App.tsx                 # Main app component
│   ├── main.tsx                # Entry point
│   └── index.css               # Global styles
├── public/                      # Static assets
├── package.json
├── vite.config.ts              # Vite configuration
├── tsconfig.json               # TypeScript config
├── tailwind.config.js          # Tailwind config
└── postcss.config.js           # PostCSS config
```

## Getting Started

### Prerequisites
- Node.js 18 or higher
- npm or yarn

### Installation

```bash
# Install dependencies
npm install
```

### Development

```bash
# Start development server
npm run dev
```

The app will be available at `http://localhost:3000`

### Build

```bash
# Build for production
npm run build
```

The build output will be in the `dist/` directory.

### Preview Production Build

```bash
# Preview production build locally
npm run preview
```

## Features

### File Upload
- Drag & drop interface
- Multiple input methods (File, URL, Inline JSON)
- Support for CSV, JSON, XLSX files
- Real-time validation
- Analysis options (PII check, Bias check)

### Results Display
- Overall health score with visual gauge
- Quality level badges (Excellent/Good/Fair/Poor/Critical)
- Six quality dimension metrics with progress bars
- Detailed column profiles with expandable rows
- Issue detection with severity levels
- PII findings with recommendations
- Duplicate analysis
- Actionable recommendations

### User Experience
- Responsive design (mobile, tablet, desktop)
- Modern, clean UI with gradients
- Loading states with progress indicators
- Error handling with user-friendly messages
- Export functionality (JSON download)
- Interactive components (expandable tables)

## Components

### Header
App branding and navigation.

### FileUpload
Three-mode upload interface:
1. **File Upload**: Drag & drop or click to browse
2. **URL Link**: Enter a direct link to data
3. **Inline JSON**: Paste JSON data directly

### LoadingSpinner
Animated loading state with progress steps.

### AnalysisResults
Container for all result components with export and reset actions.

### HealthScoreCard
Circular progress gauge showing overall health score.

### MetricsGrid
Grid of cards for six quality dimensions with scores and details.

### IssuesList
List of detected issues with severity indicators.

### ColumnProfilesTable
Expandable table showing detailed column statistics.

### RecommendationsList
Numbered list of actionable recommendations.

## API Integration

The frontend communicates with the backend through Axios:

```typescript
// Upload file
const result = await uploadFile(file, performPIICheck, performBiasCheck);

// Analyze from URL
const result = await analyzeFromUrl(url, performPIICheck, performBiasCheck);

// Analyze inline JSON
const result = await analyzeFromInline(jsonData, performPIICheck, performBiasCheck);
```

## Styling

### Tailwind CSS
The app uses Tailwind CSS for styling with custom configuration:

```javascript
// Custom colors
colors: {
  primary: {
    50: '#f0f9ff',
    // ... full palette
    900: '#0c4a6e',
  }
}
```

### Custom CSS Classes
Defined in `src/index.css`:
- `.btn` - Base button styles
- `.btn-primary` - Primary button
- `.btn-secondary` - Secondary button
- `.card` - Card container
- `.input` - Input field

## Type Safety

Full TypeScript support with defined interfaces:

```typescript
interface DataQualityResponse {
  analysisId: string;
  timestamp: string;
  healthScore: number;
  qualityLevel: QualityLevel;
  // ... more properties
}
```

## State Management

Uses React's built-in state management:
- `useState` for component state
- `useCallback` for memoized callbacks
- Props drilling for data flow (consider Context API for larger apps)

## Error Handling

Comprehensive error handling:
- API errors caught and displayed to user
- Form validation before submission
- JSON parsing validation
- Network error recovery

## Performance

### Optimizations
- Lazy loading of components (can be added)
- Memoization with `useCallback`
- Efficient re-renders
- Code splitting with Vite

### Bundle Size
- Tree shaking enabled
- Production builds minified
- Unused CSS purged by Tailwind

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Customization

### Colors
Edit `tailwind.config.js` to customize the color scheme.

### Branding
Update `src/components/Header.tsx` for logo and branding.

### Layout
Modify `src/App.tsx` for layout changes.

## Development Tips

### Hot Module Replacement
Vite provides instant HMR for fast development.

### DevTools
React DevTools recommended for debugging.

### Linting
ESLint configured for TypeScript:
```bash
npm run lint
```

## Deployment

### Static Hosting
Deploy the `dist/` folder to:
- Netlify
- Vercel
- GitHub Pages
- AWS S3 + CloudFront
- Azure Static Web Apps

### Environment Variables
Create `.env` files for different environments:

```env
VITE_API_BASE_URL=https://api.example.com
```

Access in code:
```typescript
const apiUrl = import.meta.env.VITE_API_BASE_URL;
```

## Troubleshooting

### Common Issues

1. **Port already in use**: Change port in `vite.config.ts`
2. **API connection failed**: Check backend is running and CORS is configured
3. **Build fails**: Clear `node_modules` and reinstall

### Debug Mode
Enable debug mode by setting localStorage:
```javascript
localStorage.setItem('debug', 'true');
```

## Testing (Future Enhancement)

Add tests using:
- **Vitest**: Unit testing
- **React Testing Library**: Component testing
- **Playwright**: E2E testing

## Contributing

When contributing to the frontend:

1. Follow React best practices
2. Use TypeScript for all new code
3. Follow existing component patterns
4. Test in multiple browsers
5. Ensure responsive design

## License

Proprietary software. All rights reserved.

