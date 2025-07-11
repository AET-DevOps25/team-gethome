FROM node:23-alpine AS build
WORKDIR /app

# Copy package files first for better caching
COPY package*.json ./

# Install dependencies with optimization flags
RUN npm ci --ignore-scripts --no-audit --no-fund

# Copy source code and build with optimization
COPY . .
RUN GENERATE_SOURCEMAP=false npm run build

FROM nginx:stable-alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]