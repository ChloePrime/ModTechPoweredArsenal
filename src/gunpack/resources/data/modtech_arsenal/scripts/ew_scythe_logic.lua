local M = {}

local function ifelse(bool, a, b)
    if (bool) then
        return a
    else
        return b
    end
end

local function get_cost_per_shot(api)
    return ifelse(api:getAttachment("EXTENDED_MAG") == "modtech_arsenal:energy_mod_plasma", 4, 1)
end

local function is_heat_disabled(api)
    return api:getAttachment("EXTENDED_MAG") == "modtech_arsenal:energy_mod_wave"
end

function M.shoot(api)
    if (api:isShootingNeedConsumeAmmo()) then
        local consumes = get_cost_per_shot(api)
        local success = 0
        for i = 1, consumes do
            success = success + ifelse(api:reduceAmmoOnce(), 1, 0)
        end

        if (success == 0) then
            return
        end
    end
    -- 调用射击逻辑
    api:shootOnce(false)
end

function M.handle_shoot_heat(api)
    if (is_heat_disabled(api)) then
        return
    end
    if api:hasHeatData() then
        local heatMax = api:getHeatMax()
        local heat = math.min(api:getHeatAmount() + api:getHeatPerShot() * get_cost_per_shot(api), heatMax)
        api:setHeatAmount(heat)
        if heat >= heatMax then
            api:setOverheatLocked(true);
        end
    end
end

return M