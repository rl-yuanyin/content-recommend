-- Correct image metadata using verified Pexels content classifications.
-- Existing IDs: 1-50. User upload ID 51 is preserved. New IDs: 52-77.

USE content_recommend;

SET NAMES utf8mb4;

-- Add image categories. The existing table is named category.
INSERT INTO category (id, name, sort, status)
VALUES
    (9, '游戏', 9, 1),
    (10, '动漫', 10, 1),
    (11, '历史', 11, 1),
    (12, '书籍', 12, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sort = VALUES(sort),
    status = VALUES(status);

-- Replace four photo IDs whose actual content did not match the original category.
UPDATE image
SET
    url = 'https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
    thumbnail_url = 'https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg'
WHERE id = 10;

UPDATE image
SET
    url = 'https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
    thumbnail_url = 'https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg'
WHERE id = 16;

UPDATE image
SET
    url = 'https://images.pexels.com/photos/617278/pexels-photo-617278.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
    thumbnail_url = 'https://images.pexels.com/photos/617278/pexels-photo-617278.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg'
WHERE id = 19;

UPDATE image
SET
    url = 'https://images.pexels.com/photos/325185/pexels-photo-325185.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
    thumbnail_url = 'https://images.pexels.com/photos/325185/pexels-photo-325185.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg'
WHERE id = 25;

-- Align existing image metadata with verified visual content.
UPDATE image SET category_id=1, title='阿尔卑斯山脉风光', description='高山与湖泊构成的壮丽自然景观', tags='风景,山脉,湖泊' WHERE id=1;
UPDATE image SET category_id=1, title='林间湖泊', description='森林与湖水相映的自然风景', tags='风景,森林,湖泊' WHERE id=2;
UPDATE image SET category_id=1, title='湖畔与长桥', description='湖泊、远山和桥梁组成的宁静风景', tags='风景,湖泊,桥梁' WHERE id=3;
UPDATE image SET category_id=1, title='山谷湖岸', description='开阔山谷中的湖泊与草地', tags='风景,山谷,湖岸' WHERE id=4;
UPDATE image SET category_id=2, title='风衣人物', description='穿着风衣的人物肖像', tags='人物,服饰,肖像' WHERE id=5;
UPDATE image SET category_id=1, title='阿尔卑斯雪峰', description='雪峰与高山草甸景观', tags='风景,雪山,自然' WHERE id=6;
UPDATE image SET category_id=1, title='湖畔远山', description='湖岸、山谷与远山风景', tags='风景,湖岸,远山' WHERE id=7;
UPDATE image SET category_id=2, title='商务男士肖像', description='衬衫与领带造型的男性肖像', tags='人物,男性,商务' WHERE id=8;
UPDATE image SET category_id=2, title='职场人物肖像', description='现代职业人物形象', tags='人物,职场,肖像' WHERE id=9;
UPDATE image SET category_id=2, title='女性肖像', description='自然光下的女性人物肖像', tags='人物,女性,肖像' WHERE id=10;
UPDATE image SET category_id=2, title='商务男士', description='西装与领带造型的男性肖像', tags='人物,男性,商务' WHERE id=11;
UPDATE image SET category_id=2, title='休闲服饰人物', description='穿着休闲服装的人物形象', tags='人物,服饰,生活' WHERE id=12;
UPDATE image SET category_id=2, title='时尚人物', description='现代服饰与时尚造型', tags='人物,时尚,服饰' WHERE id=13;
UPDATE image SET category_id=3, title='金毛寻回犬', description='草地上的金毛寻回犬', tags='动物,狗,宠物' WHERE id=14;
UPDATE image SET category_id=3, title='雄狮', description='自然状态下的狮子特写', tags='动物,狮子,野生动物' WHERE id=15;
UPDATE image SET category_id=3, title='宠物犬伙伴', description='两只可爱的宠物犬', tags='动物,狗,宠物' WHERE id=16;
UPDATE image SET category_id=3, title='萨摩耶犬', description='白色萨摩耶犬的正面特写', tags='动物,狗,萨摩耶' WHERE id=17;
UPDATE image SET category_id=1, title='湖畔自然风景', description='湖岸、石墙和草地构成的自然景观', tags='风景,湖岸,自然' WHERE id=18;
UPDATE image SET category_id=3, title='宠物犬肖像', description='宠物犬的温暖肖像', tags='动物,狗,宠物' WHERE id=19;
UPDATE image SET category_id=4, title='窗户与空间结构', description='建筑窗户与室内光影结构', tags='建筑,空间,窗户' WHERE id=20;
UPDATE image SET category_id=4, title='建筑与海景', description='临海建筑与开阔水域', tags='建筑,海景,工程' WHERE id=21;
UPDATE image SET category_id=4, title='古典建筑与街景', description='具有历史感的建筑与城市空间', tags='建筑,城市,街景' WHERE id=22;
UPDATE image SET category_id=4, title='工程起重机', description='大型工程机械设备', tags='建筑,工程,机械' WHERE id=23;
UPDATE image SET category_id=4, title='湖畔木屋', description='临水木屋与自然建筑景观', tags='建筑,木屋,湖畔' WHERE id=24;
UPDATE image SET category_id=4, title='现代住宅建筑', description='现代建筑立面与居住空间', tags='建筑,住宅,现代' WHERE id=25;
UPDATE image SET category_id=5, title='冰淇淋甜点', description='精致的冰淇淋甜品', tags='美食,甜点,冰淇淋' WHERE id=26;
UPDATE image SET category_id=5, title='家常馅饼', description='蔬菜与肉类制作的家常料理', tags='美食,料理,馅饼' WHERE id=27;
UPDATE image SET category_id=5, title='芝士汉堡', description='夹有芝士和蔬菜的汉堡', tags='美食,汉堡,快餐' WHERE id=28;
UPDATE image SET category_id=5, title='烤肉料理', description='盘中烤肉与配菜', tags='美食,烤肉,料理' WHERE id=29;
UPDATE image SET category_id=5, title='户外餐厅', description='户外餐饮空间与美食场景', tags='美食,餐厅,户外' WHERE id=30;
UPDATE image SET category_id=5, title='墨西哥卷饼', description='包含肉类和蔬菜的卷饼', tags='美食,卷饼,料理' WHERE id=31;
UPDATE image SET category_id=6, title='数字时钟与显示设备', description='数字显示与电子设备特写', tags='科技,电子,显示设备' WHERE id=32;
UPDATE image SET category_id=6, title='桌面电脑工作站', description='显示器、桌面与电脑设备', tags='科技,电脑,工作站' WHERE id=33;
UPDATE image SET category_id=6, title='移动设备与网页', description='移动终端上的网站与应用界面', tags='科技,移动设备,互联网' WHERE id=34;
UPDATE image SET category_id=6, title='多屏电脑设备', description='电脑显示器与桌面设备', tags='科技,电脑,显示器' WHERE id=35;
UPDATE image SET category_id=6, title='网络与科学结构', description='具有科技感的网络与科学结构', tags='科技,网络,科学' WHERE id=36;
UPDATE image SET category_id=4, title='宫殿建筑', description='具有古典风格的宫殿建筑', tags='建筑,宫殿,历史' WHERE id=37;
UPDATE image SET category_id=7, title='创意拼贴艺术', description='包含绘画工具与拼贴元素的艺术作品', tags='艺术,拼贴,创意' WHERE id=38;
UPDATE image SET category_id=7, title='花卉艺术摄影', description='以雏菊为主题的艺术摄影', tags='艺术,花卉,摄影' WHERE id=39;
UPDATE image SET category_id=3, title='海洋生物', description='海洋生物与水下生态特写', tags='动物,海洋,生态' WHERE id=40;
UPDATE image SET category_id=7, title='生态艺术摄影', description='蜜蜂与花朵构成的生态影像', tags='艺术,生态,摄影' WHERE id=41;
UPDATE image SET category_id=1, title='海岸灯塔', description='海岸、湖面与灯塔景观', tags='风景,海岸,灯塔' WHERE id=42;
UPDATE image SET category_id=7, title='艺术图案', description='具有装饰感的艺术图案', tags='艺术,图案,设计' WHERE id=43;
UPDATE image SET category_id=8, title='雨中建筑剪影', description='雨伞与建筑构成的生活影像', tags='其他,建筑,雨景' WHERE id=44;
UPDATE image SET category_id=8, title='棒球运动', description='棒球运动员与比赛场景', tags='其他,体育,棒球' WHERE id=45;
UPDATE image SET category_id=8, title='花园景观', description='花盆、围栏与花园空间', tags='其他,花园,生活' WHERE id=46;
UPDATE image SET category_id=8, title='时尚配饰', description='帽子、墨镜与服饰配饰', tags='其他,时尚,配饰' WHERE id=47;
UPDATE image SET category_id=2, title='休闲服饰人物', description='穿着休闲服装的人物', tags='人物,服饰,生活' WHERE id=48;
UPDATE image SET category_id=1, title='湖畔风景', description='湖岸与自然风景', tags='风景,湖泊,自然' WHERE id=49;
UPDATE image SET category_id=8, title='烛光静物', description='蜡烛与静物构成的生活场景', tags='其他,静物,生活' WHERE id=50;

-- Add 26 verified Pexels images for the new categories.
INSERT INTO image (
    id, title, description, url, thumbnail_url, category_id,
    author_id, author_name, tags, width, height, file_size,
    view_count, like_count, collect_count, download_count,
    feature_extracted, status
)
VALUES
    (52, '游戏手柄特写', '白色游戏手柄与电子设备', 'https://images.pexels.com/photos/442576/pexels-photo-442576.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/442576/pexels-photo-442576.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 9, 1, 'admin', '游戏,手柄,电竞', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (53, '复古游戏摇杆', '经典摇杆控制器特写', 'https://images.pexels.com/photos/275033/pexels-photo-275033.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/275033/pexels-photo-275033.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 9, 2, 'test', '游戏,摇杆,复古', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (54, '手柄与游戏设备', '游戏控制器和桌面设备', 'https://images.pexels.com/photos/687811/pexels-photo-687811.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/687811/pexels-photo-687811.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 9, 3, 'alice', '游戏,设备,娱乐', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (55, '游戏控制器', '控制器与游戏操作设备', 'https://images.pexels.com/photos/3945683/pexels-photo-3945683.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/3945683/pexels-photo-3945683.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 9, 4, 'bob', '游戏,控制器,电竞', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (56, '电竞麦克风与设备', '电竞直播使用的麦克风和输入设备', 'https://images.pexels.com/photos/7862502/pexels-photo-7862502.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/7862502/pexels-photo-7862502.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 9, 5, 'carol', '游戏,电竞,直播', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (57, '电竞桌面与显示器', '鼠标、显示器与游戏电脑桌面', 'https://images.pexels.com/photos/7915357/pexels-photo-7915357.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/7915357/pexels-photo-7915357.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 9, 6, 'david', '游戏,电脑,电竞', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (58, '角色扮演羽翼', '角色扮演服装中的羽翼道具', 'https://images.pexels.com/photos/731217/pexels-photo-731217.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/731217/pexels-photo-731217.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 10, 7, 'emma', '动漫,角色扮演,道具', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (59, '动漫玩偶收藏', '动漫主题玩偶和收藏品', 'https://images.pexels.com/photos/2695675/pexels-photo-2695675.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/2695675/pexels-photo-2695675.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 10, 8, 'frank', '动漫,玩偶,收藏', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (60, '角色扮演礼服', '角色扮演场景中的礼服造型', 'https://images.pexels.com/photos/1681010/pexels-photo-1681010.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/1681010/pexels-photo-1681010.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 10, 9, 'grace', '动漫,角色扮演,服饰', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (61, '二次元主题服饰', '具有角色扮演风格的服装和披肩', 'https://images.pexels.com/photos/6146923/pexels-photo-6146923.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/6146923/pexels-photo-6146923.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 10, 10, 'henry', '动漫,服饰,角色扮演', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (62, '动漫创意小物', '动漫主题创意摆件和道具', 'https://images.pexels.com/photos/7130560/pexels-photo-7130560.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/7130560/pexels-photo-7130560.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 10, 1, 'admin', '动漫,摆件,创意', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (63, '动漫场景道具', '舞台和场景中的动漫主题道具', 'https://images.pexels.com/photos/2695676/pexels-photo-2695676.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/2695676/pexels-photo-2695676.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 10, 2, 'test', '动漫,场景,道具', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (64, '历史宫殿建筑', '宫殿与古典建筑群', 'https://images.pexels.com/photos/208739/pexels-photo-208739.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/208739/pexels-photo-208739.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 11, 3, 'alice', '历史,宫殿,古迹', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (65, '古典宫殿与拱顶', '古典建筑中的拱顶和装饰结构', 'https://images.pexels.com/photos/460672/pexels-photo-460672.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/460672/pexels-photo-460672.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 11, 4, 'bob', '历史,建筑,宫殿', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (66, '历史遗迹与教堂', '历史教堂、方尖碑与遗迹景观', 'https://images.pexels.com/photos/2166553/pexels-photo-2166553.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/2166553/pexels-photo-2166553.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 11, 5, 'carol', '历史,遗迹,教堂', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (67, '古典喷泉', '具有历史感的喷泉与纪念碑', 'https://images.pexels.com/photos/327509/pexels-photo-327509.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/327509/pexels-photo-327509.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 11, 6, 'david', '历史,喷泉,古迹', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (68, '古桥湖景', '湖泊与桥梁构成的历史风景', 'https://images.pexels.com/photos/358482/pexels-photo-358482.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/358482/pexels-photo-358482.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 11, 7, 'emma', '历史,桥梁,湖景', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (69, '历史航海影像', '船只与历史航海主题影像', 'https://images.pexels.com/photos/618079/pexels-photo-618079.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/618079/pexels-photo-618079.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 11, 8, 'frank', '历史,航海,船只', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (70, '图书馆书架', '图书馆中的书架与阅读空间', 'https://images.pexels.com/photos/256541/pexels-photo-256541.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/256541/pexels-photo-256541.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 12, 9, 'grace', '书籍,图书馆,阅读', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (71, '书店阅读空间', '书架、书籍与书店环境', 'https://images.pexels.com/photos/590493/pexels-photo-590493.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/590493/pexels-photo-590493.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 12, 10, 'henry', '书籍,书店,阅读', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (72, '书架与藏书', '多层书架和整齐排列的图书', 'https://images.pexels.com/photos/694740/pexels-photo-694740.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/694740/pexels-photo-694740.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 12, 1, 'admin', '书籍,书架,藏书', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (73, '图书馆学习空间', '安静宽敞的图书馆阅览环境', 'https://images.pexels.com/photos/3747468/pexels-photo-3747468.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/3747468/pexels-photo-3747468.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 12, 2, 'test', '书籍,图书馆,学习', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (74, '满墙书柜', '占据整面墙的书柜与藏书', 'https://images.pexels.com/photos/4855378/pexels-photo-4855378.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/4855378/pexels-photo-4855378.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 12, 3, 'alice', '书籍,书柜,阅读', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (75, '图书馆阅览区', '图书馆内安静的阅读与学习区域', 'https://images.pexels.com/photos/1370295/pexels-photo-1370295.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/1370295/pexels-photo-1370295.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 12, 4, 'bob', '书籍,图书馆,学习', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (76, '图书馆藏书', '图书馆书架与丰富的藏书', 'https://images.pexels.com/photos/3747505/pexels-photo-3747505.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/3747505/pexels-photo-3747505.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 12, 5, 'carol', '书籍,图书馆,藏书', 800, 600, 300000, 0, 0, 0, 0, 0, 1),
    (77, '文具与书籍', '书本、文具与桌面学习用品', 'https://images.pexels.com/photos/590016/pexels-photo-590016.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg', 'https://images.pexels.com/photos/590016/pexels-photo-590016.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg', 12, 6, 'david', '书籍,文具,学习', 800, 600, 300000, 0, 0, 0, 0, 0, 1)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    url = VALUES(url),
    thumbnail_url = VALUES(thumbnail_url),
    category_id = VALUES(category_id),
    author_id = VALUES(author_id),
    author_name = VALUES(author_name),
    tags = VALUES(tags),
    width = VALUES(width),
    height = VALUES(height),
    file_size = VALUES(file_size),
    feature_extracted = 0,
    status = VALUES(status);

-- Reset feature extraction for corrected and newly added images. ID 51 is untouched.
UPDATE image SET feature_extracted = 0 WHERE id BETWEEN 1 AND 50 OR id BETWEEN 52 AND 77;
