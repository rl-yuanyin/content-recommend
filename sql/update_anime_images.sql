-- Replace category 10 records with deterministic anime-style illustrations.
-- Source: Pollinations image service. Each URL requests JPEG output explicitly;
-- fixed seeds keep the image stable when DJL downloads it again.
-- Run POST /api/recommend/milvus/init after this script to rebuild these vectors.

START TRANSACTION;

UPDATE image
SET title = '樱花列车少女',
    description = '樱花季站台上的二次元少女与驶来的列车',
    url = 'https://image.pollinations.ai/prompt/anime%20girl%20at%20a%20train%20station%20under%20cherry%20blossoms%2C%20clean%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=5801&nologo=true&model=flux&fm=jpg',
    thumbnail_url = 'https://image.pollinations.ai/prompt/anime%20girl%20at%20a%20train%20station%20under%20cherry%20blossoms%2C%20clean%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=5801&nologo=true&model=flux&fm=jpg',
    author_name = 'Pollinations AI',
    tags = '动漫,二次元,少女,樱花,列车',
    width = 800,
    height = 600,
    file_size = NULL,
    feature_extracted = 0
WHERE id = 58 AND category_id = 10;

UPDATE image
SET title = '霓虹机甲少年',
    description = '未来都市中身着机甲的二次元少年角色',
    url = 'https://image.pollinations.ai/prompt/anime%20mecha%20boy%20in%20a%20neon%20futuristic%20city%2C%20dynamic%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=5902&nologo=true&model=flux&fm=jpg',
    thumbnail_url = 'https://image.pollinations.ai/prompt/anime%20mecha%20boy%20in%20a%20neon%20futuristic%20city%2C%20dynamic%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=5902&nologo=true&model=flux&fm=jpg',
    author_name = 'Pollinations AI',
    tags = '动漫,二次元,机甲,少年,未来都市',
    width = 800,
    height = 600,
    file_size = NULL,
    feature_extracted = 0
WHERE id = 59 AND category_id = 10;

UPDATE image
SET title = '魔法图书馆',
    description = '奇幻图书馆中阅读魔法书的二次元角色',
    url = 'https://image.pollinations.ai/prompt/anime%20mage%20reading%20a%20glowing%20book%20in%20a%20fantasy%20library%2C%20detailed%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=6003&nologo=true&model=flux&fm=jpg',
    thumbnail_url = 'https://image.pollinations.ai/prompt/anime%20mage%20reading%20a%20glowing%20book%20in%20a%20fantasy%20library%2C%20detailed%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=6003&nologo=true&model=flux&fm=jpg',
    author_name = 'Pollinations AI',
    tags = '动漫,二次元,魔法,图书馆,奇幻',
    width = 800,
    height = 600,
    file_size = NULL,
    feature_extracted = 0
WHERE id = 60 AND category_id = 10;

UPDATE image
SET title = '海边夏日角色',
    description = '蓝天海岸背景下的清新二次元人物插画',
    url = 'https://image.pollinations.ai/prompt/anime%20friends%20on%20a%20sunny%20seaside%2C%20bright%20summer%20colors%2C%20clean%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=6104&nologo=true&model=flux&fm=jpg',
    thumbnail_url = 'https://image.pollinations.ai/prompt/anime%20friends%20on%20a%20sunny%20seaside%2C%20bright%20summer%20colors%2C%20clean%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=6104&nologo=true&model=flux&fm=jpg',
    author_name = 'Pollinations AI',
    tags = '动漫,二次元,夏日,海边,人物',
    width = 800,
    height = 600,
    file_size = NULL,
    feature_extracted = 0
WHERE id = 61 AND category_id = 10;

UPDATE image
SET title = '雨夜城市侦探',
    description = '雨夜街道中调查线索的二次元侦探角色',
    url = 'https://image.pollinations.ai/prompt/anime%20detective%20in%20a%20rainy%20city%20at%20night%2C%20cinematic%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=6205&nologo=true&model=flux&fm=jpg',
    thumbnail_url = 'https://image.pollinations.ai/prompt/anime%20detective%20in%20a%20rainy%20city%20at%20night%2C%20cinematic%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=6205&nologo=true&model=flux&fm=jpg',
    author_name = 'Pollinations AI',
    tags = '动漫,二次元,侦探,雨夜,城市',
    width = 800,
    height = 600,
    file_size = NULL,
    feature_extracted = 0
WHERE id = 62 AND category_id = 10;

UPDATE image
SET title = '云端幻想城堡',
    description = '飞行岛屿与云端城堡构成的二次元幻想场景',
    url = 'https://image.pollinations.ai/prompt/anime%20fantasy%20castle%20on%20floating%20islands%20above%20the%20clouds%2C%20vibrant%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=6306&nologo=true&model=flux&fm=jpg',
    thumbnail_url = 'https://image.pollinations.ai/prompt/anime%20fantasy%20castle%20on%20floating%20islands%20above%20the%20clouds%2C%20vibrant%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=6306&nologo=true&model=flux&fm=jpg',
    author_name = 'Pollinations AI',
    tags = '动漫,二次元,城堡,云海,幻想',
    width = 800,
    height = 600,
    file_size = NULL,
    feature_extracted = 0
WHERE id = 63 AND category_id = 10;

COMMIT;
