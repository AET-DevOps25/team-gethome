import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Box,
    Button,
    Container,
    Typography,
    Grid,
    Paper,
    Stack,
    Chip,
} from '@mui/material';
import { styled } from '@mui/material/styles';

const HeroSection = styled(Box)(({ theme }) => ({
    backgroundColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    padding: theme.spacing(8, 0),
    marginBottom: theme.spacing(8),
}));

const FeatureCard = styled(Paper)(({ theme }) => ({
    padding: theme.spacing(4),
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    textAlign: 'center',
}));

const LandingPage: React.FC = () => {
    const navigate = useNavigate();

    const features = [
        {
            title: 'Smart Home Management',
            description: 'Control and monitor your home devices from anywhere in the world.',
        },
        {
            title: 'Personalized Experience',
            description: 'Get recommendations and settings tailored to your preferences.',
        },
        {
            title: 'Secure & Private',
            description: 'Your data is encrypted and protected with enterprise-grade security.',
        },
    ];

    return (
        <Box>
            <HeroSection>
                <Container maxWidth="lg">
                    <Grid container spacing={4} alignItems="center">
                        <Grid item xs={12} md={6}>
                            <Typography variant="h2" component="h1" gutterBottom>
                                Welcome to GetHome
                            </Typography>
                            <Typography variant="h5" paragraph>
                                Your intelligent home management solution
                            </Typography>
                            <Stack direction="row" spacing={2}>
                                <Button
                                    variant="contained"
                                    color="secondary"
                                    size="large"
                                    onClick={() => navigate('/register')}
                                >
                                    Get Started
                                </Button>
                                <Button
                                    variant="outlined"
                                    color="inherit"
                                    size="large"
                                    onClick={() => navigate('/login')}
                                >
                                    Sign In
                                </Button>
                            </Stack>
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <Box
                                component="img"
                                src="/hero-image.png"
                                alt="Smart Home"
                                sx={{
                                    width: '100%',
                                    maxWidth: 500,
                                    height: 'auto',
                                    display: { xs: 'none', md: 'block' },
                                }}
                            />
                        </Grid>
                    </Grid>
                </Container>
            </HeroSection>

            <Container maxWidth="lg" sx={{ mb: 8 }}>
                <Typography variant="h3" component="h2" align="center" gutterBottom>
                    Why Choose GetHome?
                </Typography>
                <Grid container spacing={4} sx={{ mt: 2 }}>
                    {features.map((feature, index) => (
                        <Grid item xs={12} md={4} key={index}>
                            <FeatureCard elevation={3}>
                                <Typography variant="h5" component="h3" gutterBottom>
                                    {feature.title}
                                </Typography>
                                <Typography color="text.secondary">
                                    {feature.description}
                                </Typography>
                            </FeatureCard>
                        </Grid>
                    ))}
                </Grid>
            </Container>

            <Box sx={{ bgcolor: 'grey.100', py: 8 }}>
                <Container maxWidth="lg">
                    <Typography variant="h3" component="h2" align="center" gutterBottom>
                        Ready to Get Started?
                    </Typography>
                    <Box sx={{ textAlign: 'center', mt: 4 }}>
                        <Button
                            variant="contained"
                            color="primary"
                            size="large"
                            onClick={() => navigate('/register')}
                        >
                            Create Your Account
                        </Button>
                    </Box>
                </Container>
            </Box>

            <Container maxWidth="lg" sx={{ mb: 8 }}>
                <Typography variant="h3" component="h2" align="center" gutterBottom>
                    Features
                </Typography>
                <Grid container spacing={4} sx={{ mt: 2 }}>
                    <Grid item xs={12} md={6}>
                        <Chip
                            label="Safe Route Planning"
                            color="primary"
                            variant="filled"
                            sx={{ mb: 2 }}
                        />
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <Chip
                            label="AI Safety Companion"
                            color="secondary"
                            variant="filled"
                            sx={{ mb: 2 }}
                        />
                    </Grid>
                </Grid>
            </Container>
        </Box>
    );
};

export default LandingPage; 